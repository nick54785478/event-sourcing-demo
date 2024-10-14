package com.example.demo.iface.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RenameBookResource {

	private String bookId;
	private String externalBookId;
	
	private String name;

	public RenameBookResource(String bookId, String externalBookId) {
		this.bookId = bookId;
		this.externalBookId = externalBookId;
	}
}
