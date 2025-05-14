package com.example.demo.infra.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.util.ClassParseUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaEventPublisher {

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	/**
	 * 發布 Event
	 * 
	 * @param topic Topic
	 * @param event Event
	 */
	public void publish(String topic, BaseEvent event) {
		kafkaTemplate.send(topic, ClassParseUtil.serialize(event));
		log.debug("發布事件，message: {}", event);
	}
	
	/**
	 * 發布 Event
	 * 
	 * @param topic Topic
	 * @param eventBody Event
	 */
	public void publish(String topic, String eventBody) {
		kafkaTemplate.send(topic, eventBody);
		log.debug("發布事件，message: {}", eventBody);
	}

}
