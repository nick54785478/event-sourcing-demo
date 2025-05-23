package com.example.demo.iface.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.infra.event.BookEventStoreAdapter;
import com.example.demo.infra.repository.SnapshotRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SnapshotSchedule {
	
	private SnapshotRepository snapshotRepository;
	private BookEventStoreAdapter bookEventStoreService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void saveBookSnapshots() {
    	// TODO 定期儲存快照的排程方法，每天凌晨 12 點執行
    	
    }
	
	
}
