package com.obj.nc.functions.processors;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.event.Event;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class EventIdGenerator {
	
	private @Autowired ValidateAndGenerateEventId fn;
	
	@Bean
	public Function<Flux<Event>, Flux<Event>> validateAndGenerateEventId() {
		return eventFlux -> eventFlux.map(event -> fn.apply(event));
	}
	
	@Component
	public static class ValidateAndGenerateEventId implements Function<Event, Event> {

		@DocumentProcessingInfo("ValidateAndGenerateEventId")
		public Event apply(Event event) {
			log.debug("Validating {}",  event);
			
			event.getHeader().generateAndSetID();
			event.getHeader().setEventId(event.getHeader().getId());
	
			return event;
		}
	}
}
