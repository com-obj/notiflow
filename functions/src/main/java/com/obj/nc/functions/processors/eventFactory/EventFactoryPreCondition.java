package com.obj.nc.functions.processors.eventFactory;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;

@Component
public class EventFactoryPreCondition implements PreCondition<GenericEvent> {

	@Override
	public Optional<PayloadValidationException> apply(GenericEvent emitEventDto) {
		if (emitEventDto == null) {
			return Optional.of(new PayloadValidationException("Generic event must not be null"));
		}

		return Optional.empty();
	}

}