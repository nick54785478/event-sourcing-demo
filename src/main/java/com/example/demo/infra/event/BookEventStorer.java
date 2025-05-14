package com.example.demo.infra.event;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.ReadResult;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.StreamNotFoundException;
import com.example.demo.base.infra.event.EventStoreTemplate;
import com.example.demo.base.util.ClassParseUtil;
import com.example.demo.domain.book.aggregate.Book;
import com.example.demo.domain.snapshot.Snapshot;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BookEventStorer extends EventStoreTemplate {

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
		EventData eventData = EventData.builderAsJson(prefix, book).eventId(UUID.randomUUID()).build();
		// 存入 Event Store DB (key, data)
		eventStoreDBClient.appendToStream(eventStreamId, eventData).get();
	}

	/**
	 * 建立 SnapShot(快照)
	 * 
	 * @param snapshot
	 */
	public void createSnapshot(Snapshot snapshot) throws Throwable {
		Book book = ClassParseUtil.unserialize(snapshot.getState(), Book.class);
		// 儲存快照到 Event Store DB
		EventData snapshotEventData = EventData.builderAsJson("Snapshot", book).eventId(UUID.randomUUID()).build();
		// 寫入快照流
		String snapshotStreamId = book.getClass().getSimpleName() + "-" + snapshot.getAggregateId() + "_snapshot";
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
			}else {
				List<ResolvedEvent> snapshotEvents = snapshot.get().getEvents();
				// 取得最新的快照
				return snapshotEvents.get(snapshotEvents.size() - 1);
			}
			
		} catch (InterruptedException | ExecutionException | StreamNotFoundException e) {
			log.info("沒有快照，回傳 null，重頭執行 Replay", e);
			return null;
		}
	}

}
