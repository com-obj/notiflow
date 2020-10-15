package com.obj.nc.functions.processors.koderia;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.EmailRecipient;
import com.obj.nc.domain.DeliveryOptions;
import com.obj.nc.domain.DeliveryOptions.AGGREGATION_TYPE;
import com.obj.nc.domain.event.Event;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class FindRecepientsUsingKoderiaSubsription {
	
	//IMERATIVE start
//	@Bean
//	public Function<Event, Event> resolveRecipients() {
//		return FindRecepientsUsingKoderiaSubsription::resolveRecipients;
//	}
	//IMERATIVE END
	
	//REACTIVE START
	@Bean
	public Function<Flux<Event>, Flux<Event>> resolveRecipients() {
		return eventFlux -> eventFlux.map(event-> resolveRecipients(event));
	}	
	//REACTIVE END

	public Event resolveRecipients(Event event) {
		event.stepStart("FindRecepientsUsingKoderiaSubsription");
		
		@SuppressWarnings("unchecked")
		Optional<List<String>> technologies = Optional.of((List<String>)event.getBody().getAttributes().get("technologies"));
		
		if (!technologies.isPresent()) {
			return event;
		}
		
		//find recipients based on technologies
		EmailRecipient recipient1 = EmailRecipient.create("John Doe", "john.doe@objectify.sk");
		EmailRecipient recipient2 = EmailRecipient.create("John Dudly", "john.dudly@objectify.sk");
		
		DeliveryOptions deliveryOptions = new DeliveryOptions();
		deliveryOptions.setAggregationType(AGGREGATION_TYPE.ONCE_A_WEEK);
		recipient2.setDeliveryOptions(deliveryOptions);

		event.getBody().addRecipient(recipient1).addRecipient(recipient2);

		event.stepFinish();
		return event;
	}
}
