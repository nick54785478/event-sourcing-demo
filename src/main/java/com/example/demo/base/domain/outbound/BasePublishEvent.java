package com.example.demo.base.domain.outbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasePublishEvent {
	
	private String topic;
	
	private String partitionIndex;
	
	private String event;
}
