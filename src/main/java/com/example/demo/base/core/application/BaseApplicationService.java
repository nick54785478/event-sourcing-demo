package com.example.demo.base.core.application;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.application.port.ApplicationEventPublisher;
import com.example.demo.base.core.infra.EventLogRepository;
import com.example.demo.base.kernel.domain.EventLog;
import com.example.demo.base.kernel.domain.event.BaseEvent;
import com.example.demo.base.kernel.domain.event.BasePublishEvent;
import com.example.demo.base.kernel.util.BaseDataTransformer;
import com.example.demo.base.kernel.util.ObjectMapperUtil;

/**
 * Base Application Service
 */
@Service
public abstract class BaseApplicationService {

	@Autowired
	protected ApplicationEventPublisher eventPublisher;
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
	 * @param topic    Topic 通道
	 * @param event    事件
	 * @param eventLog
	 */
	public void publishEvent(String topic, BaseEvent event, EventLog eventLog) {
		String body = ObjectMapperUtil.serialize(event);
		BasePublishEvent publishEvent = BasePublishEvent.builder().topic(topic).event(body).build();
		eventPublisher.publish(publishEvent);
		eventLog.publish(body);
		eventLogRepository.save(eventLog);
	}

	/**
	 * 建立 EventLog
	 * 
	 * @param topic Topic
	 * @param event 事件
	 * @return eventLog
	 */
	public EventLog generateEventLog(String topic, BaseEvent event) {
		// 建立 EventLog
		EventLog eventLog = EventLog.builder().uuid(event.getEventLogUuid()).topic(topic)
				.targetId(event.getTargetId()).className(event.getClass().getName())
				.body(ObjectMapperUtil.serialize(event)).userId("SYSTEM").build();
		return eventLogRepository.save(eventLog);
	}

}
