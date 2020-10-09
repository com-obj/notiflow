package com.obj.nc.functions.processors;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.event.EmailRecipient;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.DeliveryOptions.AGGREGATION_TYPE;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class EnrichEventSettingsProcessor {

	@Bean
	public Function<Event, Event> enrichEvent() {
		return EnrichEventSettingsProcessor::enrichEvent;
	}

	public static Event enrichEvent(Event event) {
		log.debug("enrichEvent triggered");

		EmailRecipient recipient1 = EmailRecipient.create("John Doe", "john.doe@objectify.sk");

		EmailRecipient recipient2 = EmailRecipient.create("John Dudly", "john.dudly@objectify.sk");
		recipient2.getDeliveryOptions().setAggregationType(AGGREGATION_TYPE.ONCE_A_WEEK);

		event.getHeader().addRecipient(recipient1).addRecipient(recipient2);

		return event;
	}
}
