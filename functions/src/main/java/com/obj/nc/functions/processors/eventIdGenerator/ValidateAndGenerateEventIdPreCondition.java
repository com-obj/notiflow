package com.obj.nc.functions.processors.eventIdGenerator;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;

@Component
public class ValidateAndGenerateEventIdPreCondition implements PreCondition<NotificationIntent> {

	@Override
	public Optional<PayloadValidationException> apply(NotificationIntent t) {
		return Optional.empty();
	}

}