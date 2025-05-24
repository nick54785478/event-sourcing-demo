package com.example.demo.domain.book.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReprintBookCommand {

	private String bookId;
	
	private String name;
	
	private String author;
	
	private String isbn;

}
