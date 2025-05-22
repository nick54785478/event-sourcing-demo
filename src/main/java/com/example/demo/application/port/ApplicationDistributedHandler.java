package com.example.demo.application.port;

public interface ApplicationDistributedHandler {

	
	/**
	 * 嘗試取得分布式鎖
	 * */
	void acquireLock();
	
	/**
	 * 釋放分布式鎖
	 */
	void releaseLock();
}
