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
import com.example.demo.base.kernel.exception.ValidationException;
import com.example.demo.base.kernel.util.ObjectMapperUtil;
import com.example.demo.domain.book.aggregate.Book;
import com.example.demo.domain.book.command.CreateBookCommand;
import com.example.demo.domain.book.command.ReleaseBookCommand;
import com.example.demo.domain.book.command.ReplayBookCommand;
import com.example.demo.domain.book.command.ReplayBookCommand.ReplayBookEventCommand;
import com.example.demo.domain.book.command.ReplayBookCommand.ReplayBookSnapshotCommand;
import com.example.demo.domain.book.command.ReprintBookCommand;
import com.example.demo.domain.book.command.UpdateBookCommand;
import com.example.demo.domain.service.BookService;
import com.example.demo.domain.share.BookCreatedData;
import com.example.demo.domain.share.BookReplayedData;
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
	 * 更版 Book 資料
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
	 * 釋放舊版本，儲存領域事件資料
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
	 * 更新書本資料，非更版，故沒有更新版本號
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
	 * 處理新增事件
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

				// ReplayBookCommand
				// 轉換快取資料
				if (!Objects.isNull(snapshotResource)) {
					ReplayBookSnapshotCommand bookSnapshot = ObjectMapperUtil
							.unserialize(snapshotResource.getEventData(), ReplayBookSnapshotCommand.class);
					List<BaseSnapshotResource> events = (List<BaseSnapshotResource>) eventStoreAdapter
							.readEvents(readCommand);
					// 重現上一版資料
					List<ReplayBookEventCommand> replayBookEventCommands = events.stream().map(event -> {
						return ObjectMapperUtil.unserialize(event.getEventData(), ReplayBookEventCommand.class);
					}).sorted(Comparator.comparing(ReplayBookEventCommand::getVersion)).collect(Collectors.toList());

					// 轉換為 Replay Book Command
					ReplayBookCommand replayCommand = ReplayBookCommand.builder().snapshot(bookSnapshot)
							.events(replayBookEventCommands).build();
					BookReplayedData recoveredBook = bookService.replay(replayCommand);

					// 比對兩者 => 建立 UpdatedMap
					Map<String, Object> updatedMap = bookService.compareAggregateRoot(recoveredBook, book);
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

	/**
	 * 重播 Book 資料
	 * 
	 * @param command
	 */
	public void replay(String bookUuid) {

		BaseReadEventCommand readEventCommand = BaseReadEventCommand.builder()
				.streamId(Book.class.getSimpleName() + "-" + bookUuid).build();

		try {
			// 取出最新的快取
			BaseSnapshotResource snapshotResource = eventStoreAdapter.readSnapshot(readEventCommand);

			if (Objects.isNull(snapshotResource)) {
				log.error("查無快照， Snapshot 快照資料已遺失。");
				throw new ValidationException("VALIDATION_EXCEPTION", "查無快取，EventDB 資料已遺失。");
			}

			ReplayBookSnapshotCommand bookSnapshot = ObjectMapperUtil.unserialize(snapshotResource.getEventData(),
					ReplayBookSnapshotCommand.class);
			List<BaseSnapshotResource> events = (List<BaseSnapshotResource>) eventStoreAdapter
					.readEvents(readEventCommand);
			// 重現上一版資料
			List<ReplayBookEventCommand> replayBookEventCommands = events.stream().map(event -> {
				return ObjectMapperUtil.unserialize(event.getEventData(), ReplayBookEventCommand.class);
			}).sorted(Comparator.comparing(ReplayBookEventCommand::getVersion)).collect(Collectors.toList());

			// 轉換為 Replay Book Command
			ReplayBookCommand replayCommand = ReplayBookCommand.builder().snapshot(bookSnapshot)
					.events(replayBookEventCommands).build();

			// 重播後復原 Book 資料
			bookService.replayAndRecover(replayCommand);

		} catch (InterruptedException | ExecutionException e) {
			log.error("發生錯誤，復原失敗");
		}

	}

}
