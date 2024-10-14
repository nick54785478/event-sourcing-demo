package com.example.demo.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.base.exception.ValidationException;
import com.example.demo.base.service.BaseDomainService;
import com.example.demo.domain.book.aggregate.Book;
import com.example.demo.domain.book.command.CreateBookCommand;
import com.example.demo.domain.book.command.ReleaseBookCommand;
import com.example.demo.domain.book.command.RenameBookCommand;
import com.example.demo.domain.book.command.ReplayBookCommand;
import com.example.demo.domain.book.command.UpdateBookCommand;
import com.example.demo.domain.share.BookCreatedData;
import com.example.demo.domain.share.BookQueriedData;
import com.example.demo.domain.share.BookRenamedData;
import com.example.demo.domain.share.BookUpdatedData;
import com.example.demo.infra.event.BookEventStoreService;
import com.example.demo.infra.repository.BookRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class BookService extends BaseDomainService {

	private BookRepository bookRepository;
	private BookEventStoreService bookEventStoreService;

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
		return new BookCreatedData(saved.getUuid());
	}

	/**
	 * 更新版本號
	 */
	public void release(ReleaseBookCommand command) {
		// 取得本次交易 Aggregate
		Optional<Book> opt = bookRepository.findById(command.getBookId());
		if (!opt.isPresent()) {
			log.error(String.format("book not found (%s)", command.getBookId()));
		} else {
			Book book = opt.get();
			book.release(command);
			Book saved = bookRepository.save(book);
			
			try {
				bookEventStoreService.appendBookEvent(saved);
			} catch (Throwable e) {
				log.error("紀錄 EventSourcing 發生錯誤", e);
			}
			
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
			// 叫用 Command Handler
			Book book = opt.get();
			book.update(command);
			Book saved = bookRepository.save(book);
			return new BookUpdatedData(saved.getUuid());
		}
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

	/**
	 * Rename a Book
	 * 
	 * @param command
	 */
	public BookRenamedData rename(RenameBookCommand command) {

		Optional<Book> opt = bookRepository.findById(command.getBookId());
		if (!opt.isPresent()) {
			throw new ValidationException("VALIDATE_FAILED", String.format("book not found (%s)", command.getBookId()));
		} else {
			// 叫用 Command Handler
			Book book = opt.get();
			book.rename(command);
			Book saved = bookRepository.save(book);
			return new BookRenamedData(saved.getUuid());
		}

	}

	/**
	 * Replay Book Data with EventSourcing
	 * 
	 * @param commands
	 */
	public void replay(List<ReplayBookCommand> commands) {
		commands.stream().forEach(command -> {
			Book book = new Book();
			book.replay(command);
			bookRepository.save(book);
		});
	}
}
