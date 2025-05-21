package com.example.demo.base.infra.event;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.domain.outbound.BaseEvent;
import com.example.demo.base.entity.EventIdempotentLog;
import com.example.demo.base.infra.repository.EventIdempotentLogRepository;

/**
 * Event Idempotent Service 用於執行冪等機制的 Service，防止重複消費的副作用
 */
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.DEFAULT, timeout = 3600, rollbackFor = Exception.class)
public class EventIdempotentHandler {

	@Autowired
	private EventIdempotentLogRepository repository;

	/**
	 * 執行 Event 的冪等機制
	 * 
	 * @param event
	 * @return boolean
	 */
	public boolean handleIdempotency(BaseEvent event) {
		boolean result = false;
		List<EventIdempotentLog> logList = repository.findByEventTypeAndUniqueKey(event.getClass().getName(),
				event.getEventLogUuid());
		// 若查無資料
		if (logList.isEmpty()) {
			repository.insert(event.getClass().getName(), event.getEventLogUuid(), event.getTargetId());
			result = true;
		}
		return result;
	}
}
