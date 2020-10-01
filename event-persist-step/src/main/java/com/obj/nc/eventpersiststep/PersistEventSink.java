package com.obj.nc.eventpersiststep;

import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.event.Event;

@Configuration
public class PersistEventSink {
	
	private static final Logger logger = LoggerFactory.getLogger(PersistEventSink.class);
	
	@Bean
	public Consumer<Event> logEvent() {
		return event -> logger.info(event.toString());
	}

}
