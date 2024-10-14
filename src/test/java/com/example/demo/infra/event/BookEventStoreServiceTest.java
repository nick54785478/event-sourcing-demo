package com.example.demo.infra.event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.eventstore.dbclient.ResolvedEvent;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.domain.book.aggregate.Book;
import com.example.demo.util.ClassParseUtil;

@SpringBootTest
class BookEventStoreServiceTest {

	@Autowired
	private BookEventStoreService bookEventStoreService;

	@Test
	void test() throws Throwable {
		String aggrgateId = "MoneyAccount-504327e3-6c12-4672-9c6f-1280a10f3736";

		BaseEvent event = BaseEvent.builder().targetId(UUID.randomUUID().toString()).eventLogUuid(UUID.randomUUID().toString()).build();
		bookEventStoreService.appendEvent(aggrgateId, event);
	}
	

	@Test
	void testAppendBookEvent() throws Throwable {
		String uuid = UUID.randomUUID().toString();
		Book book = new Book(uuid, uuid, "水滸傳", "施耐庵", "9789869442060",  new ArrayList<>());
		bookEventStoreService.appendBookEvent(book);
	}
	
	@Test
	void testReadEvents() throws Throwable {
		List<ResolvedEvent> events = bookEventStoreService.readEvents("Book-1baacd25-2cac-41db-b5f0-2f3dd09329b9");
		events.stream().forEach(resolvedEvent -> {
			byte[] eventData = resolvedEvent.getEvent().getEventData();
			Book book = ClassParseUtil.unserialize(eventData, Book.class);
			System.out.println(resolvedEvent.getEvent().getEventId()+" : "+ClassParseUtil.serialize(book));
		});
		System.out.println("events: "+events);
	}

}
