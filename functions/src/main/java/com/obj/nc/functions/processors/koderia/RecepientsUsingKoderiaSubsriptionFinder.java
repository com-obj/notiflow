package com.obj.nc.functions.processors.koderia;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Group;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.domain.endpoints.DeliveryOptions.AGGREGATION_TYPE;
import com.obj.nc.domain.event.Event;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class RecepientsUsingKoderiaSubsriptionFinder {
	
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
		Person person1 = new Person("John Doe");
		Person person2 = new Person("John Dudly");
		Person person3 = new Person("Jonson and johnson");
		Group allObjectifyGroup = Group.createWithMembers("All Objectify", person1, person2, person3);
		
		EmailEndpoint recipient1 = EmailEndpoint.createForPerson(person1, "john.doe@objectify.sk");
		EmailEndpoint recipient2 = EmailEndpoint.createForPerson(person2, "john.dudly@objectify.sk");
		EmailEndpoint recipient3 = EmailEndpoint.createForGroup(allObjectifyGroup, "all@objectify.sk");
		
		DeliveryOptions deliveryOptions = new DeliveryOptions();
		deliveryOptions.setAggregationType(AGGREGATION_TYPE.ONCE_A_WEEK);
		recipient2.setDeliveryOptions(deliveryOptions);

		event.getBody().addAllRecipient(recipient1,recipient2,recipient3);

		event.stepFinish();
		return event;
	}
}
