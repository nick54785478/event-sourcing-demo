package com.example.demo.base.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.base.entity.EventLog;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.repository.EventLogRepository;
import com.example.demo.base.util.BaseDataTransformer;
import com.example.demo.base.util.ClassParseUtil;
import com.example.demo.infra.event.KafkaEventPublisher;

/**
 * Base Application Service
 */
@Service
public abstract class BaseApplicationService {

	@Autowired
	protected KafkaEventPublisher kafkaEventPublisher;
	@Autowired
	protected EventLogRepository eventLogRepository;

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
	 * 發布事件 (Event)
	 * 
	 * @param topic Topic 通道
	 * @param event      事件
	 * @param eventLog
	 */
	public void publishEvent(String topic, BaseEvent event, EventLog eventLog) {
		kafkaEventPublisher.publish(topic, event);
		String body = ClassParseUtil.serialize(event);
		eventLog.publish(body);
		eventLogRepository.save(eventLog);
	}

	/**
	 * 建立 EventLog
	 * 
	 * @param topicQueue Topic 通道
	 * @param event      事件
	 * @return eventLog
	 */
	public EventLog generateEventLog(String topicQueue, BaseEvent event) {
		// 建立 EventLog
		EventLog eventLog = EventLog.builder().uuid(event.getEventLogUuid()).topic(topicQueue).targetId(event.getTargetId())
				.className(event.getClass().getName()).body(ClassParseUtil.serialize(event)).userId("SYSTEM").build();
		return eventLogRepository.save(eventLog);
	}

}
