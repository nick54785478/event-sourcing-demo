package com.example.demo.base.core.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.base.core.infra.EventLogRepository;
import com.example.demo.base.core.infra.EventSourceRepository;
import com.example.demo.base.kernel.domain.EventLog;
import com.example.demo.base.kernel.domain.event.BaseEvent;
import com.example.demo.base.kernel.util.BaseDataTransformer;
import com.example.demo.base.kernel.util.ClassParseUtil;

/**
 * Base Domain Service
 */
@Service
public abstract class BaseDomainService {

	@Autowired
	protected EventLogRepository eventLogRepository;
	@Autowired
	protected EventSourceRepository eventSourceRepository;

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * 
	 * @param <T>
	 * @param target 目標物件
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件
	 */
	public <T> T transformEntityToData(Object target, Class<T> clazz) {
		return BaseDataTransformer.transformData(target, clazz);
	}

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * 
	 * @param <S,    T>
	 * 
	 * @param target 目標物件列表
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件列表
	 */
	public <S, T> List<T> transformEntityToData(List<S> target, Class<T> clazz) {
		return BaseDataTransformer.transformData(target, clazz);
	}

	/**
	 * 建立 EventLog
	 * 
	 * @param topicQueue   Topic 通道
	 * @param eventLogUuid EventLog 的 UUID
	 * @param targetId     目標物 UUID
	 * @param event        事件
	 * @return EventLog
	 */
	public EventLog generateEventLog(String topicQueue, String eventLogUuid, String targetId, BaseEvent event) {
		// 建立 EventLog
		EventLog eventLog = EventLog.builder().uuid(eventLogUuid).topic(topicQueue).targetId(targetId)
				.className(event.getClass().getName()).body(ClassParseUtil.serialize(event)).userId("SYSTEM").build();
		return eventLogRepository.save(eventLog);
	}
}
