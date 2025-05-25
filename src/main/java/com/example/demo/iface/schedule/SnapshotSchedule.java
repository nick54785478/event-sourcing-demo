package com.example.demo.iface.schedule;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.application.port.ApplicationEventStorer;
import com.example.demo.domain.book.aggregate.Book;
import com.example.demo.infra.repository.BookRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SnapshotSchedule {

	private BookRepository bookRepository;
	private ApplicationEventStorer<Book> bookEventStoreAdapter;

	@Scheduled(cron = "0 0 0 * * ?")
	public void saveBookSnapshots() {
		// 定期儲存快照的排程方法，每天凌晨 12 點執行，儲存 version>=10 且 為 10 倍數的 Book 資料
		List<Book> books = bookRepository.findVersionGreaterThanEqual10AndDivisibleBy10();
		books.stream().forEach(book -> {
			try {
				bookEventStoreAdapter.createSnapshot(book);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // 建議補上這一行以保留中斷狀態
				log.error("執行緒被中斷，無法完成儲存事件流程", e);

			} catch (ExecutionException e) {
				log.error("執行事件儲存時發生非同步執行錯誤，可能是內部執行失敗", e);
			}
		});

	}

}
