package com.example.demo.domain.book.outbound;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/*
 * 放置Create事件參數
 * */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookStoredEventData {

	private String bookId;
	private String couponNo;
	
	public BookStoredEventData(String bookId) {
		this.bookId = bookId;
	}
	
	

}
