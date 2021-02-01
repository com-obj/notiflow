package com.obj.nc.functions.processors.eventGenerator;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.event.Event;
import com.obj.nc.functions.ProcessingFunction;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
public class ValidateAndGenerateEventIdProcessingFunction extends ProcessingFunction<Event, Event, ValidateAndGenerateEventIdPreCondition> {
	@Autowired
	private Execution execution;

	@Autowired
	private ValidateAndGenerateEventIdPreCondition checkPreConditions;

	@Override
	public ValidateAndGenerateEventIdPreCondition preCondition() {
		return checkPreConditions;
	}

	@Override
	public Function<Event, Event> execution() {
		return execution;
	}
	
	@Component
	@Log4j2
	public static class Execution implements Function<Event, Event> {

		@DocumentProcessingInfo("ValidateAndGenerateEventId")
		public Event apply(Event event) {
			log.debug("Validating {}",  event);
			
			event.getHeader().generateAndSetID();
			event.getHeader().setEventId(event.getHeader().getId());
	
			return event;
		}
	}

}