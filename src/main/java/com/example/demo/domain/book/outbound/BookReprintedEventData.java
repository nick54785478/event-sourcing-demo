package com.example.demo.domain.book.outbound;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookReprintedEventData {

	private String bookId;
	private String couponNo;
	
	public BookReprintedEventData(String bookId) {
		this.bookId = bookId;
	}
	
	

}
