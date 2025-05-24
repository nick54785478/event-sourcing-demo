package com.example.demo.iface.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Resource class for the Command API
 */
@Getter
@Setter
public class ReprintBookResource {

	private String bookId;

	@NotBlank
	private String name;
	@NotBlank
	private String author;
	@NotBlank
	private String isbn;
	@NotBlank
	private String label;

	public ReprintBookResource(String name, String author, String isbn, String label) {
		this.name = name;
		this.author = author;
		this.isbn = isbn;
		this.label = label;
	}
}
