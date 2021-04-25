package com.obj.nc.koderia.functions.processors;

import static com.obj.nc.koderia.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.koderia.dto.EmitEventDto;

@Component
public class KoderiaRecipientsPreCondition implements PreCondition<NotificationIntent> {

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public Optional<PayloadValidationException> apply(NotificationIntent notificationIntent) {
		boolean eventContainsOriginalEvent = notificationIntent.getBody().containsAttributes(Collections.singletonList(ORIGINAL_EVENT_FIELD));

		if (!eventContainsOriginalEvent) {
			return Optional.of(new PayloadValidationException(String.format("NotificationIntent %s does not contain required attributes." +
					" Required attributes are: %s", notificationIntent.toString(), Collections.singletonList(ORIGINAL_EVENT_FIELD))));
		}

		Object originalEvent = notificationIntent.getBody().getAttributes().get(ORIGINAL_EVENT_FIELD);

		try {
			EmitEventDto originalEventAsDto = objectMapper.convertValue(originalEvent, EmitEventDto.class);
		} catch (IllegalArgumentException e) {
			return Optional.of(new PayloadValidationException(e.getMessage()));
		}

		return Optional.empty();
	}

}