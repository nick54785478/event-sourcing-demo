package com.example.demo.domain.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.base.core.domain.service.BaseDomainService;
import com.example.demo.base.kernel.exception.ValidationException;
import com.example.demo.domain.book.aggregate.Book;
import com.example.demo.domain.book.command.ReplayBookCommand;
import com.example.demo.domain.share.BookQueriedData;
import com.example.demo.infra.repository.BookRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class BookService extends BaseDomainService {

	private BookRepository bookRepository;

	/**
	 * 復原資料
	 * 
	 * @param command
	 */
	public void replayAndRecover(ReplayBookCommand command) {
		Book book = new Book();
		// 從快照回復資料
		book.recover(command.getSnapshot());
		book.apply(command.getEvents());
		bookRepository.save(book);
	}

	/**
	 * Find a Book By Id
	 * 
	 * @param bookId
	 * @return BookQueryData
	 */
	public BookQueriedData queryById(String bookId) {
		Optional<Book> book = bookRepository.findById(bookId);
		if (book.isPresent()) {
			var entity = book.get();
			BookQueriedData data = this.transformEntityToData(entity, BookQueriedData.class);
			return data;
		} else {
			log.error(String.format("book not found (%s)", bookId));
			throw new ValidationException(ValidationException.VALIDATE_FAILED,
					String.format("book not found (%s)", bookId));
		}

	}

}
