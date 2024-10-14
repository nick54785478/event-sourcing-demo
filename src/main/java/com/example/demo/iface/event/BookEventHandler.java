package com.example.demo.iface.event;

import java.util.Objects;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.demo.base.service.BaseEventHandler;
import com.example.demo.domain.book.command.ReleaseBookCommand;
import com.example.demo.domain.book.outbound.BookStoredEvent;
import com.example.demo.domain.book.outbound.BookStoredEventData;
import com.example.demo.service.BookCommandService;
import com.example.demo.util.BaseDataTransformer;
import com.example.demo.util.ClassParseUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BookEventHandler extends BaseEventHandler {

	@Autowired
	private BookCommandService bookCommandService;

	/**
	 * 接收 Event
	 * 
	 * @param data  消費者接收到的一條消息的具體數據結構，包含了消息的內容和一些元數據。
	 * @param ack   用來手動確認消息處理完成。
	 * @param topic 表示消息所屬的 Kafka 主題名稱
	 */
	@KafkaListener(topics = "${kafka.book.topic.name}", groupId = "${kafka.book.group.id}")
	public void bookTopic(ConsumerRecord<?, ?> data, Acknowledgment ack,
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		// @Header 提取 Kafka 消息中的頭部資訊，這裡的 KafkaHeaders.RECEIVED_TOPIC 表示消息所屬的 Kafka 主題名稱。

		log.info("Topic: {}, EventData:{}", topic, data);

		if (!Objects.isNull(data.value())) {
			BookStoredEvent event = ClassParseUtil.unserialize((String) data.value(), BookStoredEvent.class);
			BookStoredEventData eventData = event.getData();
			// 防腐處理
			ReleaseBookCommand command = BaseDataTransformer.transformData(eventData, ReleaseBookCommand.class);

			// 冪等機制，防止重覆消費所帶來的副作用
			this.checkEventIdempotency(event);

			// 呼叫 Application Service
			bookCommandService.release(command);
			// 確認消費，更新 EventLog
			this.consumeEvent(event.getEventLogUuid());
			// 消費者在成功處理訊息後，調用確認操作來通知訊息隊列，訊息已成功處理。訊息隊列訊息標記為已消費，然後將其刪除
			ack.acknowledge(); // 手動 Ack: 標記為"已消費
		}
		log.info("Kafka 消費成功! Topic:{}, Message:{}", topic, data);
	}

	

}