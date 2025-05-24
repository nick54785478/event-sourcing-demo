package com.example.demo.base.core.application;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.application.service.BookCommandService;
import com.example.demo.domain.book.aggregate.Book;

@SpringBootTest
class BaseApplicationServiceTest {
	
	@Autowired
	private BookCommandService applicationService;

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testCompareAggregateRoot() {
		Book book1 = new Book();
		Book book2 = new Book("uuid-1", "", "name", "author", "isbn", 1);
		Map<String, Object> map = applicationService.compareAggregateRoot(book1, book2);
		System.out.println(map);
		
		
	}

}
