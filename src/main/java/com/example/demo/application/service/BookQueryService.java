package com.example.demo.application.service;

import org.springframework.stereotype.Service;

import com.example.demo.base.core.application.BaseApplicationService;
import com.example.demo.base.kernel.exception.ValidationException;
import com.example.demo.domain.book.aggregate.Book;
import com.example.demo.infra.repository.BookRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class BookQueryService extends BaseApplicationService {

	private BookRepository bookRepository;

	/**
	 * Find a Book
	 * 
	 * @param bookId
	 * @return Book
	 */
	public Book queryByBookId(String bookId) {
		return bookRepository.findById(bookId).orElseThrow(() -> {
			log.error(String.format("book not found (%s)", bookId));
			throw new ValidationException(ValidationException.VALIDATE_FAILED,
					String.format("book not found (%s)", bookId));
		});

	}

}
