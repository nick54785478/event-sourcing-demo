package com.example.demo.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.eventstore.dbclient.StreamNotFoundException;
import com.example.demo.application.port.ApplicationEventStorer;
import com.example.demo.base.core.application.BaseApplicationService;
import com.example.demo.base.kernel.config.context.ContextHolder;
import com.example.demo.base.kernel.domain.EventLog;
import com.example.demo.base.kernel.domain.event.BaseEvent;
import com.example.demo.base.kernel.domain.event.BaseReadEventCommand;
import com.example.demo.base.kernel.domain.event.BaseSnapshotResource;
import com.example.demo.base.kernel.util.ObjectMapperUtil;
import com.example.demo.domain.book.aggregate.Book;
import com.example.demo.domain.book.command.ApplyBookCommand;
import com.example.demo.domain.book.command.CreateBookCommand;
import com.example.demo.domain.book.command.ReleaseBookCommand;
import com.example.demo.domain.book.command.ReprintBookCommand;
import com.example.demo.domain.book.command.UpdateBookCommand;
import com.example.demo.domain.service.BookService;
import com.example.demo.domain.share.BookCreatedData;
import com.example.demo.domain.share.BookReprintedData;
import com.example.demo.domain.share.BookUpdatedData;
import com.example.demo.infra.repository.BookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class BookCommandService extends BaseApplicationService {

	private final BookService bookService;
	private final BookRepository bookRepository;
	private final ApplicationEventStorer<Book> eventStoreAdapter;

	@Value("${kafka.book.topic.name}")
	private String bookTopic;

	/**
	 * 新增 Book 資料
	 * 
	 * @param command
	 * @return BookCreatedData
	 * @throws ValidationFailedException
	 */
	public BookCreatedData create(CreateBookCommand command) {
		// 新增 Book 資料
		BookCreatedData bookCreatedData = bookService.create(command);
		// 發布事件 進行 EventSourcing
		this.publishBookEvent(bookTopic);
		return bookCreatedData;
	}

	/**
	 * 更版 Book
	 * 
	 * @param command
	 * @return BookReprintedData
	 */
	public BookReprintedData reprint(ReprintBookCommand command) {
		// 執行更版動作
		BookReprintedData bookReprintedData = bookService.reprint(command);
		// 發布事件
		this.publishBookEvent(bookTopic);
		return bookReprintedData;
	}

	/**
	 * 儲存 EventSourcing
	 * 
	 * @param command
	 */
	public void release(ReleaseBookCommand command) {

		try {
			// 取得 clazz
			Class<?> clazz = Class.forName(command.getEventType());
			// 處理新增事件
			if (StringUtils.equals(clazz.getSimpleName(), "BookCreatedEvent")) {
				this.processCreateEvent(command, clazz.getSimpleName());
			} else {
				// 處理更版事件
				this.processReprintEvent(command, clazz.getSimpleName());
			}

		} catch (ClassNotFoundException e) {
			log.error("物件未發現，轉換錯誤", e);
		}

	}

	/**
	 * Service Command method to update
	 * 
	 * @param command
	 * @return BookUpdatedData
	 */
	public BookUpdatedData update(UpdateBookCommand command) {
		BookUpdatedData bookUpdatedData = bookService.update(command);
		return bookUpdatedData;
	}

	/**
	 * 發布 Event 到 Topic
	 */
	private void publishBookEvent(String topic) {
		BaseEvent event = ContextHolder.getEvent();
		// 寫入 EventLog（當有 Next Event 需要發佈時）
		EventLog eventLog = this.generateEventLog(topic, event);
		// 發布事件
		this.publishEvent(topic, event, eventLog);
	}


	/**
	 * 處理更版事件
	 * 
	 * @param command
	 * @param aggregateName
	 */
	private void processCreateEvent(ReleaseBookCommand command, String aggregateName) {
		// 儲存 Event Sourcing Log
		bookRepository.findById(command.getBookId()).ifPresent(book -> {
			// 將物件轉為 Map
			Map<String, Object> updatedMap = ObjectMapperUtil.convertToMap(book);
			try {
				// EventStoreDB 儲存資料
				eventStoreAdapter.appendEvent(aggregateName, book, updatedMap);

				// 第一筆存取 Snapshot，存取到 EventStoreDB
				eventStoreAdapter.createSnapshot(book);

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // 建議補上這一行以保留中斷狀態
				log.error("執行緒被中斷，無法完成儲存事件流程", e);

			} catch (ExecutionException e) {
				log.error("執行事件儲存時發生非同步執行錯誤，可能是內部執行失敗", e);
			}
		});
	}

	/**
	 * 處理更版事件
	 * 
	 * @param command
	 * @param aggregateName
	 */
	private void processReprintEvent(ReleaseBookCommand command, String aggregateName) {
		// 處理更版事件
		bookRepository.findById(command.getBookId()).ifPresent(book -> {
			String aggregateId = book.getClass().getSimpleName() + "-" + book.getUuid();

			// 重現上一次的資料
			try {
				// 讀取快取
				BaseReadEventCommand readCommand = BaseReadEventCommand.builder().streamId(aggregateId).build();

				BaseSnapshotResource snapshotResource = eventStoreAdapter.readSnapshot(readCommand);
				// 轉換快取資料
				if (!Objects.isNull(snapshotResource)) {
					Book bookSnapshot = ObjectMapperUtil.unserialize(snapshotResource.getEventData(), Book.class);
					List<BaseSnapshotResource> events = (List<BaseSnapshotResource>) eventStoreAdapter
							.readEvents(readCommand);

					// DTO -> Command
					// 重現上一版資料
					List<ApplyBookCommand> applyBookCommands = events.stream().map(event -> {
						return ObjectMapperUtil.unserialize(event.getEventData(), ApplyBookCommand.class);
					}).sorted(Comparator.comparing(ApplyBookCommand::getVersion)).collect(Collectors.toList());
					bookSnapshot.apply(applyBookCommands);

					// 比對兩者 => 建立 UpdatedMap
					Map<String, Object> updatedMap = compareAggregateRoot(bookSnapshot, book);

					// EventStoreDB 儲存資料
					eventStoreAdapter.appendEvent(aggregateName, book, updatedMap);

				}

			} catch (StreamNotFoundException e) {
				log.warn("無法讀取快照：找不到對應的事件流", e);

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.error("讀取快照時執行緒被中斷", e);

			} catch (ExecutionException e) {
				log.error("讀取快照時發生非同步執行錯誤", e);
			}

		});

	}

}
