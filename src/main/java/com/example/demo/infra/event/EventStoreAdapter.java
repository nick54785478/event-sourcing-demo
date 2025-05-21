package com.example.demo.infra.event;

import org.springframework.stereotype.Component;

import com.example.demo.application.port.ApplicationEventStorer;

/**
 * 與 EventSource DB 交互
 */
@Component
public abstract class EventStoreAdapter implements ApplicationEventStorer {

//	@Autowired
//	protected EventStoreDBClient eventStoreDBClient;
//
//	/**
//	 * 加入 Event
//	 * 
//	 * @param aggregate 通常為 Prefix(Entity 名) + Aggregate 的唯一鍵值
//	 * @param event     Event 資料
//	 */
//	public void appendEvent(String aggregateId, BaseEvent event) throws Throwable {
//		// 構建事件數據
//		EventData eventData = EventData.builderAsJson(event.getTargetId(), event).eventId(UUID.randomUUID()).build();
//		// 存入 Event Store DB (key, data)
//		eventStoreDBClient.appendToStream(aggregateId, eventData).get();
//	}
//
//	/**
//	 * 從指定事件流中讀取事件
//	 * 
//	 * @param streamId
//	 * @return List<ResolvedEvent> 事件列表
//	 */
//	public List<ResolvedEvent> readEvents(String streamId) throws Throwable {
//		// 從指定的事件流中讀取事件
//		ReadStreamOptions options = ReadStreamOptions.get().forwards().fromStart();
//		return eventStoreDBClient.readStream(streamId, options).get().getEvents();
//	}
//
//	/**
//	 * 從指定版本事件流中往後讀取事件
//	 * 
//	 * @param streamId 通常為 Prefix(Entity 名) + Aggregate 的唯一鍵值
//	 * @param index    版本，為 version - 1
//	 * @return List<ResolvedEvent> 事件列表
//	 */
//	public List<ResolvedEvent> readEvents(String streamId, Integer index) throws Throwable {
//		// 從指定的事件流中讀取事件
//		ReadStreamOptions options = ReadStreamOptions.get().forwards().fromStart();
//		// 獲取快照版本之後的所有事件
//		return eventStoreDBClient.readStream(streamId, options.fromRevision(index)).get().getEvents();
//
//	}
}
