package com.obj.nc.eventsettingsstep;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.event.DeliveryOptions.AGGREGATION_TYPE;
import com.obj.nc.domain.event.EmailRecipient;
import com.obj.nc.domain.event.Event;

@Configuration
public class EnrichEventSettingsProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(EnrichEventSettingsProcessor.class);


	@Bean
	public Function<Event, Event> enrichEvent() {
		return event -> {
			EmailRecipient recipient1 = EmailRecipient.create("John Doe", "john.doe@objectify.sk");
			
			EmailRecipient recipient2 = EmailRecipient.create("John Dudly", "john.dudly@objectify.sk");
			recipient2.getDeliveryOptions().setAggregationType(AGGREGATION_TYPE.ONCE_A_WEEK);
			
			event.getHeader()
				.addRecipient(recipient1)
				.addRecipient(recipient2);
				
			return event;
		};
	}
}
