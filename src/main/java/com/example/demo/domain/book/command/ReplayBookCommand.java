package com.example.demo.domain.book.command;

import java.util.Date;
import java.util.List;

import com.example.demo.domain.book.aggregate.vo.BookVersion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplayBookCommand {
	private String uuid;
	private String u;
	private String name;
	private String author;
	private String isbn;
	private List<BookVersion> versions;
	private Date createdDate; // 創建時間
	private String createdBy; // 創建者
	private Date lastUpdatedDate; // 最後異動時間
	private String lastUpdatedBy; // 最後異動者

}
