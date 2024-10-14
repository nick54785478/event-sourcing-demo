package com.example.demo.base.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.ResolvedEvent;
import com.example.demo.base.event.BaseEvent;

/**
 * 與 EventSource DB 交互
 * */
@Service
public class EventStoreService {

	@Autowired
	protected EventStoreDBClient eventStoreDBClient;

	/**
	 * 加入 Event
	 * 
	 * @param aggregate 通常為 Prefix(Entity 名) + Aggregate 的唯一鍵值
	 * @param event     Event 資料
	 */
	public void appendEvent(String aggregateId, BaseEvent event) throws Throwable {
		// 構建事件數據
		EventData eventData = EventData.builderAsJson(event.getTargetId(), event).eventId(UUID.randomUUID()).build();
		// 存入 Event Store DB (key, data)
		eventStoreDBClient.appendToStream(aggregateId, eventData).get();
	}

	/**
	 * 從指定事件流中讀取事件
	 * 
	 * @param streamName
	 * @return List<ResolvedEvent> 事件列表
	 */
	public List<ResolvedEvent> readEvents(String streamName) throws Throwable {
		// 從指定的事件流中讀取事件
		ReadStreamOptions options = ReadStreamOptions.get().forwards().fromStart();

		return eventStoreDBClient.readStream(streamName, options).get().getEvents();
	}
}
