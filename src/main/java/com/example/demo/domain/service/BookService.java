package com.example.demo.domain.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.base.core.domain.service.BaseDomainService;
import com.example.demo.base.kernel.exception.ValidationException;
import com.example.demo.domain.book.aggregate.Book;
import com.example.demo.domain.book.command.CreateBookCommand;
import com.example.demo.domain.book.command.ReplayBookCommand;
import com.example.demo.domain.book.command.ReprintBookCommand;
import com.example.demo.domain.book.command.UpdateBookCommand;
import com.example.demo.domain.share.BookCreatedData;
import com.example.demo.domain.share.BookQueriedData;
import com.example.demo.domain.share.BookReplayedData;
import com.example.demo.domain.share.BookReprintedData;
import com.example.demo.domain.share.BookUpdatedData;
import com.example.demo.infra.repository.BookRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class BookService extends BaseDomainService {

	private BookRepository bookRepository;

	/**
	 * 新增書籍資料
	 * 
	 * @param command
	 * @return BookCreatedData
	 */
	public BookCreatedData create(CreateBookCommand command) {
		// 呼叫 Command Handler
		Book book = new Book();
		book.create(command);
		Book saved = bookRepository.save(book);
		return this.transformEntityToData(saved, BookCreatedData.class);
	}

	/**
	 * 更版書籍資料
	 * 
	 * @param command
	 * @return BookUpdatedData
	 */
	public BookReprintedData reprint(ReprintBookCommand command) {
		Optional<Book> opt = bookRepository.findById(command.getBookId());
		if (!opt.isPresent()) {
			throw new ValidationException("VALIDATE_FAILED", String.format("book not found (%s)", command.getBookId()));
		} else {
			Book book = opt.get();
			book.reprint(command);
			Book saved = bookRepository.save(book);
			return this.transformEntityToData(saved, BookReprintedData.class);
		}
	}

	/**
	 * 更新書籍資料
	 * 
	 * @param command
	 * @return BookUpdatedData
	 */
	public BookUpdatedData update(UpdateBookCommand command) {
		Optional<Book> opt = bookRepository.findById(command.getBookId());
		if (!opt.isPresent()) {
			throw new ValidationException("VALIDATE_FAILED", String.format("book not found (%s)", command.getBookId()));
		} else {
			Book book = opt.get();
			book.update(command);
			Book saved = bookRepository.save(book);
			return this.transformEntityToData(saved, BookUpdatedData.class);
		}
	}

	/**
	 * 回復資料
	 * 
	 * @param command
	 */
	public BookReplayedData replay(ReplayBookCommand command) {
		Book book = new Book();
		// 從快照回復資料
		book.recover(command.getSnapshot());
		book.apply(command.getEvents());
		return this.transformEntityToData(book, BookReplayedData.class);
	}
	
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
