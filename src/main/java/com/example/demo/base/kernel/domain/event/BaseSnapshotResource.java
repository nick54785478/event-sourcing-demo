package com.example.demo.base.kernel.domain.event;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseSnapshotResource {

	private Long revision; // 對應 EventStore 的 revision

	private String eventId; // 事件的 UUID

	private String eventType; // 事件的類型

	private byte[] eventData; // 事件資料

	private ZonedDateTime created; // 事件時間（取自 created time）

}
