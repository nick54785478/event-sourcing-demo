package com.example.demo.iface.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookQueriedResource {

	@JsonProperty("bookId")
	private String uuid;
	
	private String name;
	
	private String author;
	
	private String isbn;

}
