package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.eventstore.dbclient.ResolvedEvent;
import com.example.demo.base.config.context.ContextHolder;
import com.example.demo.base.entity.EventLog;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.exception.ValidationException;
import com.example.demo.base.service.BaseApplicationService;
import com.example.demo.base.util.ClassParseUtil;
import com.example.demo.domain.book.aggregate.Book;
import com.example.demo.domain.book.command.CreateBookCommand;
import com.example.demo.domain.book.command.ReleaseBookCommand;
import com.example.demo.domain.book.command.RenameBookCommand;
import com.example.demo.domain.book.command.ReplayBookCommand;
import com.example.demo.domain.book.command.UpdateBookCommand;
import com.example.demo.domain.service.BookService;
import com.example.demo.domain.share.BookCreatedData;
import com.example.demo.domain.share.BookRenamedData;
import com.example.demo.domain.share.BookUpdatedData;
import com.example.demo.infra.event.BookEventAdapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class BookCommandService extends BaseApplicationService {

	private final BookService bookService;
	private final BookEventAdapter bookEventStoreService;

	@Value("${kafka.book.topic.name}")
	private String topic;

	/**
	 * Service Command method to create
	 * 
	 * @param command
	 * @return BookCreatedData
	 * @throws ValidationFailedException
	 */
	public BookCreatedData create(CreateBookCommand command) {
		BookCreatedData bookCreatedData = bookService.create(command);
		// 發布事件
		this.publishBookEvent();
		return bookCreatedData;
	}

	/**
	 * 更新版本資料
	 * 
	 * @param command
	 * @param body    更新的 Entity 資料
	 */
	public void release(ReleaseBookCommand command) {
		bookService.release(command);
	}

	/**
	 * Service Command method to update
	 * 
	 * @param command
	 * @return BookUpdatedData
	 */
	public BookUpdatedData update(UpdateBookCommand command) {
		// 發布事件
		BookUpdatedData bookUpdatedData = bookService.update(command);
		this.publishBookEvent();
		return bookUpdatedData;
	}

	/**
	 * 進行 Book 資料回復 (replay)
	 * 
	 * @param bookId
	 */
	public void replay(String bookId) {
		try {
			List<ResolvedEvent> events = new ArrayList<>();

			String streamId = new Book().getClass().getSimpleName() + "-" + bookId;

			// 嘗試讀取 Snapshot
			ResolvedEvent resolvedEvent = bookEventStoreService.readSnapshot(streamId);
			if (!Objects.isNull(resolvedEvent)) {
				log.debug("取得 Snapshot");
				// 取得 Snapshot
				byte[] eventData = resolvedEvent.getEvent().getEventData();
				Book book = ClassParseUtil.unserialize(eventData, Book.class);
				// 版本-1為該資料的 index (若 Version=10，則為第十筆(index = 9))
				events = bookEventStoreService.readEvents(streamId, book.getVersion() - 1);
			} else {
				log.debug("未取到 Snapshot，從頭開始 replay");
				// 若未取到，從頭開始 replay
				events = bookEventStoreService.readEvents(streamId);
			}
			log.info("events:{}", events);

			// 防腐處理
			List<ReplayBookCommand> commandList = events.stream().map(e -> {
				byte[] eventData = e.getEvent().getEventData();
				return ClassParseUtil.unserialize(eventData, ReplayBookCommand.class);
			}).collect(Collectors.toList());

			bookService.replay(commandList);
		} catch (Throwable e) {
			log.error("發生錯誤，Replay 失敗", e);
			throw new ValidationException("VALIDATE_FAILED", "發生錯誤，Replay 失敗");
		}
	}

	/**
	 * Service Command method to rename
	 * 
	 * @param command
	 * @return BookRenamedData
	 */
	public BookRenamedData rename(RenameBookCommand command) {
		BookRenamedData bookRenameData = bookService.rename(command);
		this.publishBookEvent();
		return bookRenameData;
	}

	/**
	 * 發布 Book Event
	 */
	private void publishBookEvent() {
		BaseEvent event = ContextHolder.getEvent();
		// 寫入 EventLog（當有 Next Event 需要發佈時）
		EventLog eventLog = this.generateEventLog(topic, event);
		// 發布事件
		this.publishEvent(topic, event, eventLog);
	}

}
