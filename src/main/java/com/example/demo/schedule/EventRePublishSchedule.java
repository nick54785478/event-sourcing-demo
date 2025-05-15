package com.example.demo.schedule;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.base.entity.EventLog;
import com.example.demo.base.enums.EventLogSendQueueStatus;
import com.example.demo.base.infra.repository.EventLogRepository;
import com.example.demo.infra.event.KafkaPublishAdapter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EventRePublishSchedule {

	@Autowired
	private EventLogRepository eventLogRepository;
	@Autowired
	private KafkaPublishAdapter kafkaEventPublisher;

	@Value("${kafka.book.topic.name}")
	private String topic;

	/**
	 * Event 重發布排程: 針對 Event 發布未成功的事件(3分鐘內)，將其進行重發布
	 */
	@Scheduled(fixedDelayString = "60000", initialDelay = 1000L)
	public void rePublishEvent() {
		// 往前 3 分鐘
		Date time = new Date((new Date()).getTime() - 300000L);
		List<EventLog> eventLogList = eventLogRepository.findByStatusAndOccuredAtBefore(EventLogSendQueueStatus.INITIAL,
				time);

		if (!eventLogList.isEmpty()) {
			eventLogList.stream().forEach(eventLog -> {
				log.debug("Event Data: {}", eventLog.getBody());

				// Event 重發佈
				kafkaEventPublisher.publish(topic, eventLog.getBody());
				eventLog.publish(eventLog.getBody()); // 變更狀態為: 已發布
			});
			eventLogRepository.saveAll(eventLogList);
		}
	}

}
