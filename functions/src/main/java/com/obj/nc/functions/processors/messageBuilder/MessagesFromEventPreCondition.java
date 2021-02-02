package com.obj.nc.functions.processors.messageBuilder;

import com.obj.nc.domain.event.Event;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class MessagesFromEventPreCondition implements PreCondition<Event> {

	@Override
	public Optional<PayloadValidationException> apply(Event event) {
		if (event.getBody().getRecievingEndpoints().isEmpty()) {
			return Optional.of(new PayloadValidationException(String.format("Event %s has no receiving endpoints defined.", event)));
		}

		return Optional.empty();
	}

}