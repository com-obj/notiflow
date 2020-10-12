package com.obj.nc.functions.sink;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.event.Event;

@Configuration
public class LogEventSink {
	
	private static final Logger logger = LoggerFactory.getLogger(LogEventSink.class);
	
	@Bean
	public Consumer<Event> logEvent() {
		return LogEventSink::logEvent;
	}
	
	public static void logEvent(Event event) {
		logger.info(event.toString());
	}

}
