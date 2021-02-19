package com.obj.nc.functions.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.domain.event.Event;
import com.obj.nc.dto.EmitEventDto;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.obj.nc.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;

@Component
public class KoderiaRecipientsPreCondition implements PreCondition<Event> {

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public Optional<PayloadValidationException> apply(Event event) {
		boolean eventContainsOriginalEvent = event.getBody().containsAttributes(Collections.singletonList(ORIGINAL_EVENT_FIELD));

		if (!eventContainsOriginalEvent) {
			return Optional.of(new PayloadValidationException(String.format("Event %s does not contain required attributes." +
					" Required attributes are: %s", event.toString(), Collections.singletonList(ORIGINAL_EVENT_FIELD))));
		}

		Object originalEvent = event.getBody().getAttributes().get(ORIGINAL_EVENT_FIELD);

		try {
			EmitEventDto originalEventAsDto = objectMapper.convertValue(originalEvent, EmitEventDto.class);
		} catch (IllegalArgumentException e) {
			return Optional.of(new PayloadValidationException(e.getMessage()));
		}

		return Optional.empty();
	}

}