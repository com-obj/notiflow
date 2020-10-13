package com.obj.nc.functions.processors.koderia;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.event.Event;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class GenerateProcessingIdProcessor {

	@Bean
	public Function<Event, Event> generateProcessingId() {
		return GenerateProcessingIdProcessor::generateProcessingId;
	}

	public static Event generateProcessingId(Event event) {
		log.info("Generating processingID for {}",  event);
		event.stepStart("GenerateProcessingIdProcessor");
		
		event.getHeader().generateAndSetEventID();
		
		event.stepFinish();
		return event;
	}
}
