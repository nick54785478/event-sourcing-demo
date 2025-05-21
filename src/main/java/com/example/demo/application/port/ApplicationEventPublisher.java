package com.example.demo.application.port;

import com.example.demo.base.domain.outbound.BasePublishEvent;

public interface ApplicationEventPublisher {

	void publish(BasePublishEvent event);
	
}
