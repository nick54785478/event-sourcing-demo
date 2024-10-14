package com.example.demo.infra.event;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.eventstore.dbclient.EventData;
import com.example.demo.base.service.EventStoreService;
import com.example.demo.domain.book.aggregate.Book;

@Service
public class BookEventStoreService extends EventStoreService {

	/**
	 * 加入 Event
	 * 
	 * @param root Aggregate 根實體
	 */
	public void appendBookEvent(Book book) throws Throwable {
		// 取得前綴
		String prefix = book.getClass().getSimpleName();
		// aggregate 通常為 Prefix(Entity 名) + Aggregate 的唯一鍵值
		String eventStreamId = prefix + "-" + book.getUuid();
		// 構建事件數據
		EventData eventData = EventData.builderAsJson(eventStreamId, book).eventId(UUID.randomUUID()).build();
		// 存入 Event Store DB (key, data)
		eventStoreDBClient.appendToStream(eventStreamId, eventData).get();
	}

}
