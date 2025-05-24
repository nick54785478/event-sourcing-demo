package com.example.demo.base.kernel.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseSnapshotResource {

	private String snapshotStreamId;
	
	private Object snapshot; // 快照資訊
}
