package com.example.demo.infra.event;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.base.kernel.domain.event.BaseReadEventCommand;
import com.example.demo.base.kernel.domain.event.BaseSnapshotResource;

@SpringBootTest
class BookEventStoreAdapterTest {

	@Autowired
	private BookEventStoreAdapter bookEventStoreAdapter;

	@Test
	void testReadSnapshot() throws InterruptedException, ExecutionException {
		BaseReadEventCommand command = BaseReadEventCommand.builder()
				.streamId("Book-a2aafb38-4162-496e-a5a9-c13cd92c4dc2").build();
		BaseSnapshotResource resource = bookEventStoreAdapter.readSnapshot(command);
		System.out.println(resource);
	}

	@Test
	void testReadEvents() throws Throwable {
		BaseReadEventCommand command = BaseReadEventCommand.builder()
				.streamId("Book-a2aafb38-4162-496e-a5a9-c13cd92c4dc2").build();
		List<BaseSnapshotResource> events = bookEventStoreAdapter.readEvents(command);
		System.out.println(events);
	}
}
