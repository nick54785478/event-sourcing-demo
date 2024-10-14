package com.example.demo.config;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ZooKeeperConfig {

    @Value("${zookeeper.address}")
    private String connectString;
    
    @Value("${zookeeper.timeout}")
    private int timeout;

    @Bean	
    public ZooKeeper zpClient() {
    	ZooKeeper zooKeeper = null;
    	try {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            // 連線成功後，會回呼 watcher 監聽，此連線操作是異步的，執行完 new 語句後，直接呼叫後續程式碼
            // 可指定多台服務位址 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
            zooKeeper = new ZooKeeper(connectString, timeout, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (Event.KeeperState.SyncConnected == event.getState()) {
                    	// 如果收到了服務端的回應事件,連線成功
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await();
            log.info("初始化 ZooKeeper  連接狀態 : {}", zooKeeper.getState());
        } catch (Exception e) {
            log.error("初始化 ZooKeeper 連接異常: {}", e);
        }
        return zooKeeper;
    }
}
