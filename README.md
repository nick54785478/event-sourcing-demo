
<h3>敘述</h3>
這是一個 EventSourcing 範例，使用 Apache Kafka 作為 MQ，將領域事件資料透過 MQ 存放至 EventStoreDB 中，紀錄 Book 各版本的"當時狀態"，也能夠根據需要對資料進行 Replay 回復至當前的 Book 資料。

<h3>框架及外部依賴</h3>

>* Java
>* SpringBoot 3.3.4
>* JDK 17
>* MySQL
>* Kafka & ZooLeeper 
>* Event Source DB

<h3>啟動步驟</h3>

* 第一步. 透過 Docker 啟動外部依賴，亦可本地端建置。

* 第二步. 使用 Postman 進行測試。
