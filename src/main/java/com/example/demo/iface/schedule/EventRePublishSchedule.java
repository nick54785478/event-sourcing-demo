package com.example.demo.iface.schedule;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.base.core.infra.EventLogRepository;
import com.example.demo.base.kernel.domain.BasePublishEvent;
import com.example.demo.base.kernel.domain.EventLog;
import com.example.demo.base.kernel.domain.enums.EventLogSendQueueStatus;
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

				BasePublishEvent event = BasePublishEvent.builder().topic(topic).event(eventLog.getBody()).build();
				// Event 重發佈
				kafkaEventPublisher.publish(event);
				eventLog.publish(eventLog.getBody()); // 變更狀態為: 已發布
			});
			eventLogRepository.saveAll(eventLogList);
		}
	}

}
