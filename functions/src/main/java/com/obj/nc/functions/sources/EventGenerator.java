package com.obj.nc.functions.sources;

import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.obj.nc.domain.event.Event;

@EnableScheduling
@Configuration
public class EventGenerator {
	
	@Bean
    @Scheduled(fixedDelay = 1000)
    public Supplier<Event> generateEvent() {
		return EventGenerator::generateConstantEvent;
	}
	
	public static Event generateConstantEvent() {
		Event event = Event.createWithSimpleMessage("test-config", "Hi there!!");
		return event;
	}

}
