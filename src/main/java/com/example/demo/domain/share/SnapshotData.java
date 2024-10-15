package com.example.demo.domain.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SnapshotData<T> {
    
	private String aggregateId;
	
	private String classType;
    
	private T state;
    
	private long version;

}