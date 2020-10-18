package com.obj.nc.functions.processors;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.event.Event;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class EventIdGenerator {

//IMERATIVE START
//	@Bean
//	public Function<Event, Event> generateProcessingId() {
//		return GenerateProcessingIdProcessor::generateProcessingId;
//	}
//IMERATIVE END
	
//REACTIVE START
		@Bean
	    public Function<Flux<Event>, Flux<Event>> validateAndGenerateEventId() {
			return eventFlux -> eventFlux.map(event -> validateAndGenerateEventId(event));
		}
//REACTIVE END

	public Event validateAndGenerateEventId(Event event) {
		log.debug("Validating {}",  event);
		
		event.stepStart("ValidateAndGenerateEventId");
		
		event.getHeader().generateAndSetID();
		event.getHeader().setEventId(event.getHeader().getId());
		
		event.stepFinish();
		return event;
	}
}
