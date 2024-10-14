package com.example.demo.domain.book.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RenameBookCommand {

	private String bookId;
	private String externalBookId;
	
	private String name;

	public RenameBookCommand(String bookId, String externalBookId) {
		this.bookId = bookId;
		this.externalBookId = externalBookId;
	}
}
