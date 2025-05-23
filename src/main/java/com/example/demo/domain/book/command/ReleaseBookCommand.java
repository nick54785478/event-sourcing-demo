package com.example.demo.domain.book.command;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseBookCommand {

	private String eventType;

	private String bookId;

	private String couponNo;

	private Map<String, Object> updateMap;

}
