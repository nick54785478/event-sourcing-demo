package com.example.demo.application.port;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.eventstore.dbclient.StreamNotFoundException;
import com.example.demo.base.core.domain.BaseAggregateRoot;
import com.example.demo.base.kernel.domain.event.BaseReadEventCommand;
import com.example.demo.base.kernel.domain.event.BaseSnapshotResource;

public interface ApplicationEventStorer<T extends BaseAggregateRoot> {

	/**
	 * 加入 Event
	 * 
	 * @param eventType     事件類型
	 * @param aggregateRoot 聚合根的繼承類
	 * @param updatedMap    被變更的資料
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	void appendEvent(String eventType, T aggregateRoot, Map<String, Object> updatedMap)
			throws InterruptedException, ExecutionException;

	/**
	 * 從指定版本事件流中往後讀取事件
	 * 
	 * @param resource
	 * @return List<?> 事件列表
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	List<?> readEvents(BaseReadEventCommand command) throws InterruptedException, ExecutionException;

	/**
	 * 建立快照
	 * 
	 * @param aggregateRoot 聚合根的繼承類
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	void createSnapshot(T aggregateRoot) throws InterruptedException, ExecutionException;

	/**
	 * 讀取快照
	 * 
	 * @param command 用來查詢快照 (封裝 Stream Id 、 Index)
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws StreamNotFoundException
	 */
	BaseSnapshotResource readSnapshot(BaseReadEventCommand command)
			throws InterruptedException, ExecutionException, StreamNotFoundException;

}
