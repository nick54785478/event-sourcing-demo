package com.example.demo.iface.schedule;

import java.util.List;

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
		// TODO 定期儲存快照的排程方法，每天凌晨 12 點執行
		List<Book> books = bookRepository.findVersionGreaterThanEqual10AndDivisibleBy10();
		
		
//		bookEventStoreAdapter.readEvents(null);

	}

}
