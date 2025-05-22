package com.example.demo.base.core.infra;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.base.kernel.domain.EventLog;
import com.example.demo.base.kernel.domain.enums.EventLogSendQueueStatus;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

	EventLog findByUuid(String uuid);
	
	List<EventLog> findByStatusAndOccuredAtBefore(EventLogSendQueueStatus status, Date time);

}
