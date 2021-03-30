package com.obj.nc.functions.processors.dummy;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class DummyRecepientsEnrichmentPreCondition implements PreCondition<NotificationIntent> {

	public static final List<String> REQUIRED_ATTRIBUTES = Arrays.asList("technologies");

	@Override
	public Optional<PayloadValidationException> apply(NotificationIntent notificationIntent) {
		boolean eventHasRequiredAttributes = notificationIntent.getBody().getMessage().containsNestedAttributes(REQUIRED_ATTRIBUTES, "originalEvent", "data");

		if (!eventHasRequiredAttributes) {
			return Optional.of(new PayloadValidationException(String.format("NotificationIntent %s does not contain required attributes." +
					" Required attributes are: %s", notificationIntent.toString(), REQUIRED_ATTRIBUTES)));
		}

		return Optional.empty();
	}

}