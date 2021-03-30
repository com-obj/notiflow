package com.obj.nc.functions.processors.messageBuilder;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class MessagesFromNotificationIntentPreCondition implements PreCondition<NotificationIntent> {

	@Override
	public Optional<PayloadValidationException> apply(NotificationIntent notificationIntent) {
		if (notificationIntent.getBody().getRecievingEndpoints().isEmpty()) {
			return Optional.of(new PayloadValidationException(String.format("NotificationIntent %s has no receiving endpoints defined.", notificationIntent)));
		}

		return Optional.empty();
	}

}