package com.obj.nc.eventsettingsstep;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.handler.annotation.SendTo;

import com.obj.nc.domain.event.DeliveryOptions.AGGREGATION_TYPE;
import com.obj.nc.domain.event.EmailRecipient;
import com.obj.nc.domain.event.Event;

@EnableBinding(Processor.class)
public class EnrichEventSettingsProcessor {

	
	@StreamListener(Processor.INPUT)
	@SendTo(Processor.OUTPUT)
	public Event processUsageCost(Event event) {
		
		EmailRecipient recipient1 = EmailRecipient.create("John Doe", "john.doe@objectify.sk");
		
		EmailRecipient recipient2 = EmailRecipient.create("John Dudly", "john.dudly@objectify.sk");
		recipient2.getDeliveryOptions().setAggregationType(AGGREGATION_TYPE.ONCE_A_WEEK);
		
		event.getHeader()
			.addRecipient(recipient1)
			.addRecipient(recipient2);
			
		return event;
	}
}
