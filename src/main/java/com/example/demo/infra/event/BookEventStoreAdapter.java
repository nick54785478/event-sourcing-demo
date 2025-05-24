package com.example.demo.infra.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ReadResult;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import com.example.demo.application.port.ApplicationEventStorer;
import com.example.demo.base.kernel.domain.event.BaseReadEventCommand;
import com.example.demo.base.kernel.domain.event.BaseSnapshotResource;
import com.example.demo.base.kernel.util.BaseDataTransformer;
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
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@Override
	public void appendEvent(String eventType, Book aggregateRoot, Map<String, Object> updatedMap)
			throws InterruptedException, ExecutionException {
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
	 * 建立快照
	 * 
	 * @param aggregateRoot
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@Override
	public void createSnapshot(Book aggregateRoot) throws InterruptedException, ExecutionException {
		String prefix = aggregateRoot.getClass().getSimpleName();
		String type = prefix + "-snapshot";
		String eventStreamId = prefix + "-" + aggregateRoot.getUuid() + "-snapshot";
		// 構建事件數據
		EventData eventData = EventData.builderAsJson(type, aggregateRoot).eventId(UUID.randomUUID()).build();
		eventStoreDBClient.appendToStream(eventStreamId, eventData).get();
	}

	/**
	 * 從指定版本事件流中往後讀取事件
	 * 
	 * @param resource
	 * @return List<BaseSnapshotResource> 事件列表
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@Override
	public List<BaseSnapshotResource> readEvents(BaseReadEventCommand command)
			throws InterruptedException, ExecutionException {
		// 根據是否有指定 index 來決定從哪裡開始讀取，若沒有 index， 就從事件流開頭讀到尾部（可依需求調整）
		ReadStreamOptions options = (command.getIndex() != null)
				? ReadStreamOptions.get().forwards().fromRevision(command.getIndex())
				: ReadStreamOptions.get().forwards().fromStart();
		ReadResult readResult = eventStoreDBClient.readStream(command.getStreamId(), options).get();
		return readResult.getEvents().stream()
				.map(event -> BaseDataTransformer.transformData(event.getEvent(), BaseSnapshotResource.class))
				.collect(Collectors.toList());
	}

	/**
	 * 讀取 Book 快照
	 * 
	 * @param command
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@Override
	public BaseSnapshotResource readSnapshot(BaseReadEventCommand command)
			throws InterruptedException, ExecutionException {

		// 根據是否有指定 index 來決定從哪裡開始讀取，若沒有 index， 就從事件流開頭讀到尾部（可依需求調整）
		ReadStreamOptions options = (command.getIndex() != null)
				? ReadStreamOptions.get().forwards().fromRevision(command.getIndex())
				: ReadStreamOptions.get().forwards().fromStart();
		CompletableFuture<ReadResult> snapshot = eventStoreDBClient.readStream(command.getStreamId() + "-snapshot",
				options);
		// 沒有快照
		if (Objects.isNull(snapshot)) {
			return null;
		}

		// 取得
		List<ResolvedEvent> snapshotEvents = snapshot.get().getEvents();
		// 取得最新的快照資料
		ResolvedEvent resolvedEvent = snapshotEvents.get(snapshotEvents.size() - 1);
		RecordedEvent event = resolvedEvent.getEvent();

		return BaseDataTransformer.transformData(event, BaseSnapshotResource.class);

	}

}
