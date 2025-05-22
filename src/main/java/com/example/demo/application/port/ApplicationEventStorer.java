package com.example.demo.application.port;

import java.util.List;

import com.example.demo.base.core.domain.BaseAggregateRoot;

public interface ApplicationEventStorer<T extends BaseAggregateRoot> {

	/**
	 * 加入 Event
	 * 
	 * @param aggregateRoot Aggregate Root 的繼承類
	 * @throws Throwable
	 */
	void appendEvent(T aggregateRoot) throws Throwable;

	/**
	 * 從指定事件流中讀取事件
	 * 
	 * @param streamId
	 * @return List<ResolvedEvent> 事件列表
	 * @throws Throwable
	 */
	List<?> readEvents(String streamId) throws Throwable;

	/**
	 * 從指定版本事件流中往後讀取事件
	 * 
	 * @param streamId 通常為 Prefix(Entity 名) + Aggregate 的唯一鍵值
	 * @param index    版本，為 version - 1
	 * @return List<?> 事件列表
	 * @throws Throwable
	 */
	List<?> readEvents(String streamId, Integer index) throws Throwable;

	/**
	 * 建立 SnapShot(快照)
	 * 
	 * @param aggregateRoot    Aggregate Root 的繼承類
	 * @param snapshotStreamId Snapshot Stream Id
	 */
	void createSnapshot(T aggregateRoot, String snapshotStreamId) throws Throwable;
}
