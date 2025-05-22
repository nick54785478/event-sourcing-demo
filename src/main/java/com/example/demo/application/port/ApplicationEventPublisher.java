package com.example.demo.application.port;

import com.example.demo.base.kernel.domain.event.BasePublishEvent;

public interface ApplicationEventPublisher {

	void publish(BasePublishEvent event);
	
}
