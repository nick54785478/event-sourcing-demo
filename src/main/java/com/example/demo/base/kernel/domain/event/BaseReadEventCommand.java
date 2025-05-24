package com.example.demo.base.kernel.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseReadEventCommand {

	private String streamId;  // Stream Id (通常為 AggregateId 或 Snapshot Id)
	
	private Integer index;  // version 版本號
}
