package com.example.demo.base.iface.event;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.base.entity.EventLog;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.infra.event.EventIdempotentHandler;
import com.example.demo.base.infra.repository.EventLogRepository;
import com.example.demo.base.infra.repository.EventSourceRepository;
import com.example.demo.base.util.BaseDataTransformer;
import com.example.demo.base.util.ClassParseUtil;

/**
 * Base Event Handler
 */
@Component
public class BaseEventHandler {

	@Autowired
	protected EventLogRepository eventLogRepository;
	@Autowired
	protected EventSourceRepository eventSourceRepository;
	@Autowired
	protected EventIdempotentHandler eventIdempotentHandler;

	/**
	 * 檢查冪等
	 * 
	 * @param event
	 * @return boolean
	 */
	public boolean checkEventIdempotency(BaseEvent event) {
		return eventIdempotentHandler.handleIdempotency(event);
	}

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * 
	 * @param <T>
	 * @param target 目標物件
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件
	 */
	public <T> T transformData(Object target, Class<T> clazz) {
		return BaseDataTransformer.transformData(target, clazz);
	}

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * 
	 * @param <S,    T>
	 * @param target 目標物件列表
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件列表
	 */
	public <S, T> List<T> transformData(List<S> target, Class<T> clazz) {
		return BaseDataTransformer.transformData(target, clazz);
	}

	/**
	 * 建立 EventLog
	 * 
	 * @param topicQueue Topic 通道
	 * @param event      事件
	 */
	public EventLog generateEventLog(String topicQueue, BaseEvent event) {
		// 建立 EventLog
		EventLog eventLog = EventLog.builder().uuid(event.getEventLogUuid()).topic(topicQueue)
				.targetId(event.getTargetId()).className(event.getClass().getName())
				.body(ClassParseUtil.serialize(event)).userId("SYSTEM").build();
		return eventLogRepository.save(eventLog);
	}

	/**
	 * 進行消費
	 * 
	 * @param eventLogUuid
	 */
	public void consumeEvent(String eventLogUuid) {
		// 查詢 EventLog
		EventLog eventLog = eventLogRepository.findByUuid(eventLogUuid);
		if (!Objects.isNull(eventLog)) {
			eventLog.consume(); // 更改狀態為: 已消費
			eventLogRepository.save(eventLog);
		}

	}

}
