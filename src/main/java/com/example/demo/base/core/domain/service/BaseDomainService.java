package com.example.demo.base.core.domain.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.base.core.infra.EventLogRepository;
import com.example.demo.base.core.infra.EventSourceRepository;
import com.example.demo.base.kernel.domain.EventLog;
import com.example.demo.base.kernel.domain.event.BaseEvent;
import com.example.demo.base.kernel.util.BaseDataTransformer;
import com.example.demo.base.kernel.util.ObjectMapperUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Base Domain Service
 */
@Slf4j
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
				.className(event.getClass().getName()).body(ObjectMapperUtil.serialize(event)).userId("SYSTEM").build();
		return eventLogRepository.save(eventLog);
	}
	
	/**
	 * 比對 先前資料 與 目標資料
	 * 
	 * @param source 先前資料
	 * @param target 目標資料
	 * @return Map<Entity欄位, 值>
	 */
	public Map<String, Object> compareAggregateRoot(Object source, Object target) {
		Map<String, Object> differences = new LinkedHashMap<>();
		try {
			Map<String, Object> sourceMap = ObjectMapperUtil.convertToMap(source);
			Map<String, Object> targetMap = ObjectMapperUtil.convertToMap(target);

			for (String key : sourceMap.keySet()) {
				if (targetMap.containsKey(key)) {
					Object before = sourceMap.get(key);
					Object after = targetMap.get(key);
					if (!Objects.equals(before, after)) {
						differences.put(key, after);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			log.error("物件轉換比對失敗", e);
		}
		return differences;
	}
}
