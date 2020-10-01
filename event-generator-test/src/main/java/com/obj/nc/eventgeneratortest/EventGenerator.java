package com.obj.nc.eventgeneratortest;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.obj.nc.domain.event.Event;

@EnableScheduling
@Configuration
public class EventGenerator {
	
	private static final Logger logger = LoggerFactory.getLogger(EventGenerator.class);

	@Bean
    @Scheduled(fixedDelay = 1000)
    public Supplier<Event> generateEvents() {
		return () ->  {
			Event event = Event.createWithSimpleMessage("test-config", "Hi there!!");
			return event;
		};
	}

}
