package com.obj.nc.functions.processors.koderia;

import com.obj.nc.domain.event.Event;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class RecepientsUsingKoderiaSubscriptionPreCondition implements PreCondition<Event> {

	public static final List<String> REQUIRED_ATTRIBUTES = Arrays.asList("technologies");

	@Override
	public Optional<PayloadValidationException> apply(Event event) {
		boolean eventHasRequiredAttributes = event.getBody().containsNestedAttributes("originalEvent", REQUIRED_ATTRIBUTES);

		if (!eventHasRequiredAttributes) {
			return Optional.of(new PayloadValidationException(String.format("Event %s does not contain required attributes." +
					" Required attributes are: %s", event.toString(), REQUIRED_ATTRIBUTES)));
		}

		return Optional.empty();
	}

}