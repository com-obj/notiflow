package com.obj.nc.functions.processors.koderia;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.event.EmailRecipient;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.DeliveryOptions.AGGREGATION_TYPE;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class FindRecepientsUsingKoderiaSubsription {

	@Bean
	public Function<Event, Event> resolveRecipients() {
		return FindRecepientsUsingKoderiaSubsription::resolveRecipients;
	}

	public static Event resolveRecipients(Event event) {
		event.regenerateAndSetID();
		log.info("enrichEvent triggered");
		
		@SuppressWarnings("unchecked")
		Optional<List<String>> technologies = Optional.of((List<String>)event.getBody().getAttributes().get("technologies"));
		
		if (!technologies.isPresent()) {
			return event;
		}
		
		//find recipients based on technologies
		EmailRecipient recipient1 = EmailRecipient.create("John Doe", "john.doe@objectify.sk");
		EmailRecipient recipient2 = EmailRecipient.create("John Dudly", "john.dudly@objectify.sk");
		recipient2.getDeliveryOptions().setAggregationType(AGGREGATION_TYPE.ONCE_A_WEEK);

		event.getHeader().addRecipient(recipient1).addRecipient(recipient2);

		return event;
	}
}
