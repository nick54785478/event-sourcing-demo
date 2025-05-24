package com.example.demo.domain.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookReprintedData {

	private String uuid; // PK uuid

	private String name; // 姓名

	private String author; // 作者

	private String isbn; // isbn

	private Integer version;
}
