package com.obj.nc.functions.processors.eventIdGenerator;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.event.Event;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ValidateAndGenerateEventIdExecution implements Function<Event, Event> {

	@DocumentProcessingInfo("ValidateAndGenerateEventId")
	public Event apply(Event event) {
		log.debug("Validating {}",  event);
		
		event.getHeader().generateAndSetID();
		event.getHeader().setEventId(event.getHeader().getId());

		return event;
	}
}