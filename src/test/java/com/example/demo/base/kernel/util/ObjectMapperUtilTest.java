package com.example.demo.base.kernel.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.domain.book.aggregate.Book;

class ObjectMapperUtilTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testMergeJsonIntoObject() {
		Book book = new Book();
		String jsonString = """
				{
				  "createdDate": 1748077093000,
				  "createdBy": "nick123@example.com",
				  "lastUpdatedDate": 1748077093000,
				  "lastUpdatedBy": "nick123@example.com",
				  "uuid": "a2aafb38-4162-496e-a5a9-c13cd92c4dc2",
				  "u": null,
				  "name": "西遊記(初版)",
				  "author": "沈伯洋",
				  "isbn": "9789575709518",
				  "version": 1
				}	
				
				""";
		Book mergeBook = ObjectMapperUtil.mergeJsonIntoObject(book, jsonString);
		System.out.println(mergeBook);
	}

}
