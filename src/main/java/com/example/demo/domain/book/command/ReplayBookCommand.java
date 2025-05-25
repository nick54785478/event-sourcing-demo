package com.example.demo.domain.book.command;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplayBookCommand {

	private ReplayBookSnapshotCommand snapshot;
	
	private List<ReplayBookEventCommand> events;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ReplayBookSnapshotCommand {
		
		private String uuid; // PK uuid

		private String name; // 姓名

		private String author; // 作者

		private String isbn; // isbn

		private Integer version = 0;
		
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ReplayBookEventCommand {
		
		private String name; // 姓名

		private String author; // 作者

		private String isbn; // isbn

		private Integer version; // version
	}
}


