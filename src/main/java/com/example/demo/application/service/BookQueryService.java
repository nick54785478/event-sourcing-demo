package com.example.demo.application.service;

import org.springframework.stereotype.Service;

import com.example.demo.base.core.application.BaseApplicationService;
import com.example.demo.domain.service.BookService;
import com.example.demo.domain.share.BookQueriedData;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BookQueryService extends BaseApplicationService {

	private BookService bookService;

	/**
	 * Find a Book
	 * 
	 * @param bookId
	 * @return BookQueryData
	 */
	public BookQueriedData queryById(String bookId) {
		return bookService.queryById(bookId);
	}

}
