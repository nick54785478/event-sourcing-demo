package com.example.demo.domain.snapshot.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSnapshotCommand {

	private String classType;
	
	private String aggregateId;
	
	private String state;
    
	private long version;

}
