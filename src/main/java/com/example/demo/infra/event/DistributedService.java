package com.example.demo.infra.event;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 實作 分布式系統鎖 操作的 Service
 */
@Slf4j
@Service
public class DistributedService extends ZooKeeperService {

	private static final String LOCK_ROOT_PATH = "/locks";
	private static final String LOCK_NODE_NAME = "lock_";

	private String currentLockNode; // 臨時順序節點

	/**
	 * 獲取鎖
	 */
	public void acquireLock() throws KeeperException, InterruptedException {

		// 如果父(根)節點不存在，創建它
		if (Objects.isNull(this.exists(LOCK_ROOT_PATH, false))) {
			this.createNode(LOCK_ROOT_PATH, "0", CreateMode.PERSISTENT);
		}

		// 創建臨時順序節點
		this.currentLockNode = this.createNode(LOCK_ROOT_PATH + "/" + LOCK_NODE_NAME, "0",
				CreateMode.EPHEMERAL_SEQUENTIAL);
		// /locks0000000007
		log.info("currentLockNode:{}", this.currentLockNode);
		// 嘗試獲取鎖
		attemptLock();
	}

	/**
	 * 嘗試獲取分布式鎖
	 */
	private void attemptLock() throws KeeperException, InterruptedException {
		List<String> lockNodes = this.getChildren(LOCK_ROOT_PATH);
		Collections.sort(lockNodes); // 將子節點排序
		log.debug("lockNodes: {}", lockNodes);

		int index = lockNodes.indexOf(currentLockNode.substring(LOCK_ROOT_PATH.length() + 1));

		// 如果當前節點是序號最小的，則獲取鎖
		if (index == 0) {
			log.info("獲取鎖成功: ", this.currentLockNode);
		} else {
			// 監聽比自己序號小的上一個節點
			String prevNode = lockNodes.get(index - 1);
			Stat stat = this.exists(LOCK_ROOT_PATH + "/" + prevNode, event -> {
				if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
					try {
						attemptLock();
					} catch (KeeperException | InterruptedException e) {
						log.error("取得鎖發生錯誤");
					}
				}
			});

			// 如果節點不存在了，立即嘗試重新獲取鎖
			if (stat == null) {
				attemptLock();
			}
		}
	}

	/**
	 * 釋放鎖
	 */
	public void releaseLock() throws KeeperException, InterruptedException {
		this.deleteNode(this.currentLockNode);
		log.info("釋放鎖: {}", this.currentLockNode);
	}

	/**
	 * 關閉 ZooKeeper 連接
	 */
	public void close() throws InterruptedException {
		this.close();
	}
}
