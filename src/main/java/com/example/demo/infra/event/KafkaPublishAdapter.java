package com.example.demo.infra.event;

import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.application.port.ApplicationEventPublisher;
import com.example.demo.base.kernel.domain.event.BasePublishEvent;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class KafkaPublishAdapter implements ApplicationEventPublisher {

	private KafkaTemplate<String, String> kafkaTemplate;

	/**
	 * 發布 Event
	 * 
	 * @param event
	 */
	@Override
	public void publish(BasePublishEvent event) {
		if (StringUtils.isNotBlank(event.getPartitionIndex())) {
			kafkaTemplate.send(event.getTopic(), event.getPartitionIndex(), event.getEvent());
		} else {
			kafkaTemplate.send(event.getTopic(), event.getEvent());
		}
		log.debug("發布事件 Topic:{}，Message: {}", event.getTopic(), event.getEvent());
	}

}
