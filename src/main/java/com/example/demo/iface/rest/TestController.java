package com.example.demo.iface.rest;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.base.kernel.domain.event.BaseEvent;
import com.example.demo.infra.event.BookEventAdapter;

import lombok.RequiredArgsConstructor;

@RequestMapping("/test")
@RestController
@RequiredArgsConstructor
public class TestController {
	
	@Autowired
	private BookEventAdapter bookEventStoreService;

	@PostMapping("/interactEventStoreDB")
	public void testInteractEventStoreDB() throws Throwable {
		String aggrgateId = "111";

		BaseEvent event = BaseEvent.builder().targetId(UUID.randomUUID().toString()).eventLogUuid(UUID.randomUUID().toString()).build();
		
//		bookEventStoreService.appendEvent(aggrgateId, event);
	}
}
