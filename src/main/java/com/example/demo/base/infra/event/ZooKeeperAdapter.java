package com.example.demo.base.infra.event;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 實作 Zookeeper 操作的 Service
 * */
@Slf4j
@Service
public class ZooKeeperAdapter {

	@Autowired
	private ZooKeeper zkClient;

	/**
	 * 判斷節點是否存在
	 * 
	 * @param path
	 * @param needWatch 指定是否複用 ZooKeeper 的 Watcher
	 */
	public Stat exists(String path, boolean needWatch) {
		try {
			return zkClient.exists(path, needWatch);
		} catch (Exception e) {
			log.error("指定節點是否存在異常 {},{}", path, e);
			return null;
		}
	}

	/**
	 * 偵測節點是否存在 並設定監聽事件 三種監聽類型： 創建，刪除，更新
	 *
	 * @param path
	 * @param watcher 傳入指定的監聽類
	 * @return
	 */
	public Stat exists(String path, Watcher watcher) {
		try {
			return zkClient.exists(path, watcher);
		} catch (Exception e) {
			log.error("指定節點是否存在異常 {},{}", path, e);
			return null;
		}
	}

	/**
	 * 建立持久化節點
	 *
	 * @param path
	 * @param data
	 * @param mode 
	 */
	public String createNode(String path, String data, CreateMode mode) {
		try {
			return zkClient.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
		} catch (KeeperException | InterruptedException e) {
			log.error("建立持久化節點發生異常 {},{},{}", path, data, e);
			return null;
		}
	}
	
	/**
	 * 建立持久化節點
	 *
	 * @param path
	 * @param data
	 */
	public boolean createNode(String path, String data) {
		try {
			zkClient.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		} catch (KeeperException | InterruptedException e) {
			log.error("建立持久化節點發生異常 {},{},{}", path, data, e);
			return false;
		}
		return true;
	}

	/**
	 * 修改持久化節點
	 *
	 * @param path
	 * @param data
	 * @return boolean
	 */
	public boolean updateNode(String path, String data) {
		try {
			// zk 的資料版本是從 0 開始計數的。如果客戶端傳入的是-1，則表示zk伺服器需要基於最新的資料進行更新。
			// 如果對 zk 的資料節點的更新操作沒有原子性要求則可以使用-1.
			// version 參數指定要更新的資料的版本, 如果version和真實的版本不同, 更新操作將失敗. 指定 version 為 -1 則忽略版本檢查
			zkClient.setData(path, data.getBytes(), -1);
			return true;
		} catch (KeeperException | InterruptedException e) {
			log.error("修改持久化節點發生異常 {},{},{}", path, data, e);
			return false;
		}
	}

	/**
	 * 刪除持久化節點
	 *
	 * @param path
	 * @return boolean
	 */
	public boolean deleteNode(String path) {
		try {
			// version 参数指定要更新的数据的版本, 如果version和真实的版本不同, 更新操作将失败. 指定version为-1则忽略版本检查
			zkClient.delete(path, -1);
			return true;
		} catch (Exception e) {
			log.error("刪除持久化節點發生異常 {},{}", path, e);
			return false;
		}
	}

	/**
	 * 取得目前節點的子節點(不包含孫節點)
	 *
	 * @param path 父節點path
	 */
	public List<String> getChildren(String path) throws KeeperException, InterruptedException {
		List<String> list = zkClient.getChildren(path, false);
		return list;
	}

	/**
	 * 取得指定節點的值
	 *
	 * @param path
	 * @return
	 */
	public String getData(String path, Watcher watcher) {
		try {
			Stat stat = new Stat();
			byte[] bytes = zkClient.getData(path, watcher, stat);
			return new String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 關閉連結
	 * */
	public void close() throws InterruptedException {
		zkClient.close();
	}
}
