package com.obj.nc.functions.processors;

import java.util.function.Function;

import com.obj.nc.exceptions.PayloadValidationException;
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

	private @Autowired CheckPreConditions checkPreConditions;
	
	@Bean
	public Function<Flux<Event>, Flux<Event>> validateAndGenerateEventId() {
		return eventFlux -> eventFlux.map(event -> checkPreConditions.andThen(fn).apply(event));
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

	@Component
	public static class CheckPreConditions implements Function<Event, Event> {
		@Override
		public Event apply(Event event) {
			if (event.getHeader().getEventId() != null) {
				throw new PayloadValidationException(String.format("Event {} already has EventId: {}", event, event.getHeader().getEventId()));
			}

			return event;
		}
	}
}
