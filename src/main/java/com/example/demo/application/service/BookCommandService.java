package com.example.demo.application.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.port.ApplicationEventStorer;
import com.example.demo.base.core.application.BaseApplicationService;
import com.example.demo.base.kernel.config.context.ContextHolder;
import com.example.demo.base.kernel.domain.EventLog;
import com.example.demo.base.kernel.domain.event.BaseEvent;
import com.example.demo.base.kernel.util.ObjectMapperUtil;
import com.example.demo.domain.book.aggregate.Book;
import com.example.demo.domain.book.command.CreateBookCommand;
import com.example.demo.domain.book.command.ReleaseBookCommand;
import com.example.demo.domain.book.command.RenameBookCommand;
import com.example.demo.domain.book.command.ReprintBookCommand;
import com.example.demo.domain.book.command.UpdateBookCommand;
import com.example.demo.domain.service.BookService;
import com.example.demo.domain.share.BookCreatedData;
import com.example.demo.domain.share.BookRenamedData;
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
		BookReprintedData bookReprintedData = bookService.reprint(command);
		this.publishBookEvent(bookTopic); // 發布事件
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
				// 儲存 Event Sourcing Log
				bookRepository.findById(command.getBookId()).ifPresent(book -> {
					// 將物件轉為 Map
					Map<String, Object> updatedMap = ObjectMapperUtil.convertToMap(book);
					try {
						// EventStoreDB 儲存資料
						eventStoreAdapter.appendEvent(clazz.getSimpleName(), book, updatedMap);

						// 第一筆存取 Snapshot，存取到 EventStoreDB
						eventStoreAdapter.createSnapshot(book);

					} catch (Throwable e) {
						log.error("發生錯誤，儲存 Event 失敗", e);
					}
				});

			} else {
				// 處理更版事件

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
	 * 更新 Book 名稱
	 * 
	 * @param command
	 */
	public BookRenamedData rename(RenameBookCommand command) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 比對 先前資料 與 目標資料
	 * 
	 * @param source 先前資料
	 * @param target 目標資料
	 * @return Map<Entity欄位, 值>
	 */
	private Map<String, Object> compareObjects(Object source, Object target) {
		Map<String, Object> differences = new LinkedHashMap<>();
		try {

			// 若原始 source 為 null，則視為全部欄位為變更
//			if (source == null && target != null) {
//				return ObjectMapperUtil.convertToMap(target);
//			}

			Map<String, Object> sourceMap = ObjectMapperUtil.convertToMap(source);
			Map<String, Object> targetMap = ObjectMapperUtil.convertToMap(target);

			for (String key : sourceMap.keySet()) {
				if (targetMap.containsKey(key)) {
					Object before = sourceMap.get(key);
					Object after = targetMap.get(key);
					if (!Objects.equals(before, after)) {
						differences.put(key, after);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			log.error("物件轉換比對失敗", e);
		}
		return differences;
	}

}
