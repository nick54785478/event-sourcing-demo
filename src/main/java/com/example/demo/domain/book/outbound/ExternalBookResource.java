package com.example.demo.domain.book.outbound;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resource class for the Command API
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExternalBookResource {

	private String bookId;
	private String name;

}
