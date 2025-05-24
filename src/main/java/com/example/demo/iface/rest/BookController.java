package com.example.demo.iface.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.service.BookCommandService;
import com.example.demo.application.service.BookQueryService;
import com.example.demo.base.core.iface.BaseController;
import com.example.demo.base.kernel.util.BaseDataTransformer;
import com.example.demo.domain.book.command.CreateBookCommand;
import com.example.demo.domain.book.command.RenameBookCommand;
import com.example.demo.domain.book.command.ReprintBookCommand;
import com.example.demo.domain.book.command.UpdateBookCommand;
import com.example.demo.domain.share.BookCreatedData;
import com.example.demo.domain.share.BookRenamedData;
import com.example.demo.domain.share.BookReprintedData;
import com.example.demo.domain.share.BookUpdatedData;
import com.example.demo.iface.dto.BookCreatedResource;
import com.example.demo.iface.dto.BookQueriedResource;
import com.example.demo.iface.dto.BookRenamedResource;
import com.example.demo.iface.dto.BookReplayedResource;
import com.example.demo.iface.dto.BookReprintedResource;
import com.example.demo.iface.dto.BookUpdatedResource;
import com.example.demo.iface.dto.CreateBookResource;
import com.example.demo.iface.dto.RenameBookResource;
import com.example.demo.iface.dto.ReprintBookResource;
import com.example.demo.iface.dto.UpdateBookResource;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/book")
@RestController
@RequiredArgsConstructor
public class BookController extends BaseController {

	private final BookCommandService bookCommandService;
	private final BookQueryService bookQueryService;

	/**
	 * Create a book
	 * 
	 * @param resource
	 * @return ResponseEntity<BookCreatedResource>
	 */
	@PostMapping("")
	public ResponseEntity<BookCreatedResource> create(@Valid @RequestBody CreateBookResource resource) {
		// DTO 防腐處理 (resource > Command)
		CreateBookCommand command = BaseDataTransformer.transformData(resource, CreateBookCommand.class);
		// 呼叫 Application Service
		BookCreatedData responseBody = bookCommandService.create(command);
		return new ResponseEntity<>(BaseDataTransformer.transformData(responseBody, BookCreatedResource.class),
				HttpStatus.CREATED);
	}
	
	@PostMapping("/{bookId}/reprint")
	public ResponseEntity<BookReprintedResource> reprint(@PathVariable String bookId,
			@Valid @RequestBody ReprintBookResource resource) {
		// DTO 防腐處理 (Resource > Command)
		ReprintBookCommand command = BaseDataTransformer.transformData(resource, ReprintBookCommand.class);
		command.setBookId(bookId);

		// 呼叫 Application Service
		BookReprintedData responseBody = bookCommandService.reprint(command);

		// DTO 防腐處理 (Domain > DTO)，並回傳
		return new ResponseEntity<>(BaseDataTransformer.transformData(responseBody, BookReprintedResource.class),
				HttpStatus.OK);
	}

	/**
	 * Update a book
	 * 
	 * @param bookId
	 * @param book
	 * @return ResponseEntity<BookUpdatedResource>
	 */
	@PutMapping("/{bookId}")
	public ResponseEntity<BookUpdatedResource> update(@PathVariable String bookId,
			@Valid @RequestBody UpdateBookResource resource) {
		// DTO 防腐處理 (Resource > Command)
		UpdateBookCommand command = BaseDataTransformer.transformData(resource, UpdateBookCommand.class);
		command.setBookId(bookId);

		// 呼叫 Application Service
		BookUpdatedData responseBody = bookCommandService.update(command);

		// DTO 防腐處理 (Domain > DTO)，並回傳
		return new ResponseEntity<>(BaseDataTransformer.transformData(responseBody, BookUpdatedResource.class),
				HttpStatus.OK);
	}

	/**
	 * Rename a book
	 * 
	 * @param resource
	 * @return ResponseEntity<BookRenamedResource>
	 */
	@PutMapping("/rename/{bookId}")
	public ResponseEntity<BookRenamedResource> update(@RequestBody RenameBookResource resource) {
		// DTO 防腐處理 (Resource > Command)
		RenameBookCommand command = BaseDataTransformer.transformData(resource, RenameBookCommand.class);
		// 呼叫 Application Service
		BookRenamedData responseBody = bookCommandService.rename(command);
		// DTO 防腐處理 (Domain > DTO)，並回傳
		return new ResponseEntity<>(BaseDataTransformer.transformData(responseBody, BookRenamedResource.class),
				HttpStatus.OK);
	}

	/**
	 * Query a book by its id
	 * 
	 * @param bookId
	 * @return
	 */
	@GetMapping("/{bookId}")
	public ResponseEntity<BookQueriedResource> query(@PathVariable String bookId) {

		return new ResponseEntity<>(
				BaseDataTransformer.transformData(bookQueryService.queryById(bookId), BookQueriedResource.class),
				HttpStatus.OK);
	}

	/**
	 * Replay book
	 * 
	 * @param bookId
	 * @param book
	 * @return ResponseEntity<BookUpdatedResource>
	 */
	@PostMapping("/replay/{bookId}")
	public ResponseEntity<BookReplayedResource> replay(@PathVariable String bookId) {

		// 呼叫 Application Service
//		bookCommandService.replay(bookId);
		return new ResponseEntity<>(new BookReplayedResource(bookId), HttpStatus.OK);
	}

}
