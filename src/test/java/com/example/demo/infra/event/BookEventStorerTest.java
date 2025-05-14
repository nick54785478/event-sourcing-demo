package com.example.demo.infra.event;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.eventstore.dbclient.ResolvedEvent;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.util.ClassParseUtil;
import com.example.demo.domain.book.aggregate.Book;
import com.example.demo.domain.snapshot.Snapshot;

@SpringBootTest
class BookEventStorerTest {

	@Autowired
	private BookEventStorer bookEventStoreService;

//	@Test
	void test() throws Throwable {
		String aggrgateId = "MoneyAccount-504327e3-6c12-4672-9c6f-1280a10f3736";

		BaseEvent event = BaseEvent.builder().targetId(UUID.randomUUID().toString())
				.eventLogUuid(UUID.randomUUID().toString()).build();
		bookEventStoreService.appendEvent(aggrgateId, event);
	}

//	@Test
	void testAppendBookEvent() throws Throwable {
		String uuid = UUID.randomUUID().toString();
		Book book = new Book(uuid, uuid, "水滸傳", "施耐庵", "9789869442060", 1);
		bookEventStoreService.appendBookEvent(book);
	}

//	@Test
	void testReadEvents() throws Throwable {
		List<ResolvedEvent> events = bookEventStoreService.readEvents("Book-1baacd25-2cac-41db-b5f0-2f3dd09329b9");
		events.stream().forEach(resolvedEvent -> {
			byte[] eventData = resolvedEvent.getEvent().getEventData();
			Book book = ClassParseUtil.unserialize(eventData, Book.class);
			System.out.println(resolvedEvent.getEvent().getEventId() + " : " + ClassParseUtil.serialize(book));
		});
		System.out.println("events: " + events);
	}

//	@Test
	void testCreateSnapshot() {
		String state = """
				{"createdDate":1728971638000,"createdBy":"nick123@example.com","lastUpdatedDate":1728971645000,
				"lastUpdatedBy":"nick123@example.com","uuid":"1baacd25-2cac-41db-b5f0-2f3dd09329b9","u":null,
				"name":"水滸傳(一版)","author":"施耐庵","isbn":"9789869442060","version":3}
				""";
		Snapshot snapshot = Snapshot.builder().aggregateId("1baacd25-2cac-41db-b5f0-2f3dd09329b9").classType("Book")
				.state(state).build();
		try {
			bookEventStoreService.createSnapshot(snapshot);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

//	@Test
	void testReadSnapshot() {
		var resolvedEvent = bookEventStoreService.readSnapshot("Book-1baacd25-2cac-41db-b5f0-2f3dd09329b9");
		byte[] eventData = resolvedEvent.getEvent().getEventData();
		Book book = ClassParseUtil.unserialize(eventData, Book.class);
		System.out.println("book:" + book);
		System.out.println("book name :"+book.getName());
		System.out.println("book author :"+book.getAuthor());
		System.out.println("book version :"+book.getVersion());
	}
	
	@Test
	void testReadEvents2() throws Throwable {
		var resolvedEvents = bookEventStoreService.readEvents("Book-1baacd25-2cac-41db-b5f0-2f3dd09329b9_snapshot", 1);
		
		resolvedEvents.stream().forEach(e -> {
			byte[] eventData = e.getEvent().getEventData();
			Book book = ClassParseUtil.unserialize(eventData, Book.class);
			System.out.println("book:" + book);
			System.out.println("book name :"+book.getName());
			System.out.println("book author :"+book.getAuthor());
			System.out.println("book version :"+book.getVersion());
			
		});
		
	}
	

}
