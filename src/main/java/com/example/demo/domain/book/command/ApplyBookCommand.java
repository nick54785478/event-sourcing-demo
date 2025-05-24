package com.example.demo.domain.book.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplyBookCommand {

	private String name; // 姓名

	private String author; // 作者

	private String isbn; // isbn

	private Integer version; // version
}
