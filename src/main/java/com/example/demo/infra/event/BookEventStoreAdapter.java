package com.example.demo.infra.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ReadResult;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.StreamNotFoundException;
import com.example.demo.application.port.ApplicationEventStorer;
import com.example.demo.domain.book.aggregate.Book;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BookEventStoreAdapter implements ApplicationEventStorer<Book> {

	@Autowired
	private EventStoreDBClient eventStoreDBClient;

	/**
	 * 加入 Event
	 * 
	 * @param eventType  事件類型
	 * @param root       Aggregate 根實體
	 * @param updatedMap 被變更的資料(欄位, 更改後的值)
	 */
	@Override
	public void appendEvent(String eventType, Book aggregateRoot, Map<String, Object> updatedMap) throws Throwable {
		// 建立前綴 - Book
		String prefix = aggregateRoot.getClass().getSimpleName();

		// eventStreamId 通常為 Prefix(Entity 名) + Aggregate 的唯一鍵值
		String eventStreamId = prefix + "-" + aggregateRoot.getUuid();

		// 構建事件數據
		EventData eventData = EventData.builderAsJson(eventType, updatedMap).eventId(UUID.randomUUID()).build();

		// 存入 Event Store DB (key, data)
		eventStoreDBClient.appendToStream(eventStreamId, eventData).get();

	}

	/**
	 * 建立 SnapShot(快照)
	 * 
	 * @param book
	 * @param snapshotStreamId
	 */
	@Override
	public void createSnapshot(Book book, String snapshotStreamId) throws Throwable {
		// 建立前綴 - Book
		String prefix = book.getClass().getSimpleName();

		// 儲存快照到 Event Store DB
		EventData snapshotEventData = EventData.builderAsJson(prefix + "-snapshot", book)
				.eventId(UUID.randomUUID()).build();
		// 寫入快照流
		eventStoreDBClient.appendToStream(snapshotStreamId, snapshotEventData).get();
	}

	/**
	 * 讀取快照資料
	 * 
	 * @param snapshotStreamId
	 * @return 目前最新的快照
	 */
	public ResolvedEvent readSnapshot(String aggregateId) {

		try {
			String snapshotStreamId = aggregateId + "_snapshot";
			// 從快照流讀取最新的快照
			ReadStreamOptions options = ReadStreamOptions.get().backwards().fromEnd(); // 從流的尾部開始往回讀
			CompletableFuture<ReadResult> snapshot = eventStoreDBClient.readStream(snapshotStreamId, options);

			if (Objects.isNull(snapshot)) {
				return null; // 沒有快照
			} else {
				List<ResolvedEvent> snapshotEvents = snapshot.get().getEvents();
				// 取得最新的快照
				return snapshotEvents.get(snapshotEvents.size() - 1);
			}

		} catch (InterruptedException | ExecutionException | StreamNotFoundException e) {
			log.info("沒有快照，回傳 null，重頭執行 Replay", e);
			return null;
		}
	}

	@Override
	public List<?> readEvents(String streamId) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<?> readEvents(String streamId, Integer index) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

}
