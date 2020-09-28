package com.obj.nc.eventpersiststep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import com.obj.nc.domain.event.Event;

@EnableBinding(Sink.class)
public class PersistEventSink {
	
	private static final Logger logger = LoggerFactory.getLogger(EventPersistStepApplication.class);

	@StreamListener(Sink.INPUT)
	public void process(Event event) {
		logger.info(event.toString());
	}

}
