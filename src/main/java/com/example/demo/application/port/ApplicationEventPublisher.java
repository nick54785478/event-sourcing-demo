package com.example.demo.application.port;

import com.example.demo.base.kernel.domain.BasePublishEvent;

public interface ApplicationEventPublisher {

	void publish(BasePublishEvent event);
	
}
