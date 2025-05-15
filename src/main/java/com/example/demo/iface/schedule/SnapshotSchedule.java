package com.example.demo.iface.schedule;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.domain.snapshot.Snapshot;
import com.example.demo.infra.event.BookEventStorer;
import com.example.demo.infra.repository.SnapshotRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SnapshotSchedule {
	
	private SnapshotRepository snapshotRepository;
	private BookEventStorer bookEventStoreService;

	// 定期儲存快照的排程方法，每天凌晨 12 點執行
    @Scheduled(cron = "0 0 0 * * ?")
    public void saveBookSnapshots() {
//    	bookEventStoreService.re
//    	
//    	snapshotRepository.find
//    	
//        // 查詢需要儲存快照的 Aggregate 列表
//        List<Snapshot> aggregates = getAggregatesToSnapshot();
//
//        // 儲存每個 Aggregate 的快照
//        aggregates.forEach(aggregate -> {
//            saveSnapshot(aggregate);
//        });
    }
	
	
}
