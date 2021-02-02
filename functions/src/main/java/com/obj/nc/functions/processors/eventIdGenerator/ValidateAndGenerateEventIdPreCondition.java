package com.obj.nc.functions.processors.eventIdGenerator;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.Event;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;

@Component
public class ValidateAndGenerateEventIdPreCondition implements PreCondition<Event> {

	@Override
	public Optional<PayloadValidationException> apply(Event t) {
		return Optional.empty();
	}

}