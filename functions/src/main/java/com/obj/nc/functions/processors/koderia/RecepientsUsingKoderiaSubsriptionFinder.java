package com.obj.nc.functions.processors.koderia;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.DeliveryOptions.AGGREGATION_TYPE;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Group;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.domain.event.Event;

import reactor.core.publisher.Flux;

@Configuration
public class RecepientsUsingKoderiaSubsriptionFinder {

	@Autowired
	private ResolveRecipients fn;

	@Bean
	public Function<Flux<Event>, Flux<Event>> resolveRecipients() {
		return eventFlux -> eventFlux.map(event -> fn.apply(event));
	}

	@Component
	public static class ResolveRecipients implements Function<Event, Event>{

		@DocumentProcessingInfo("FindRecepientsUsingKoderiaSubsription")
		public Event apply(Event event) {
			@SuppressWarnings("unchecked")
			Optional<List<String>> technologies = Optional
					.of((List<String>) event.getBody().getAttributes().get("technologies"));

			if (!technologies.isPresent()) {
				return event;
			}

			// find recipients based on technologies
			Person person1 = new Person("John Doe");
			Person person2 = new Person("John Dudly");
			Person person3 = new Person("Jonson and johnson");
			Group allObjectifyGroup = Group.createWithMembers("All Objectify", person1, person2, person3);

			EmailEndpoint endpoint1 = EmailEndpoint.createForPerson(person1, "john.doe@objectify.sk");
			EmailEndpoint endpoint2 = EmailEndpoint.createForPerson(person2, "john.dudly@objectify.sk");
			EmailEndpoint endpoint3 = EmailEndpoint.createForGroup(allObjectifyGroup, "all@objectify.sk");

			DeliveryOptions deliveryOptions = new DeliveryOptions();
			deliveryOptions.setAggregationType(AGGREGATION_TYPE.ONCE_A_WEEK);
			endpoint2.setDeliveryOptions(deliveryOptions);

			event.getBody().addAllRecievingEndpoints(endpoint1, endpoint2, endpoint3);

			return event;
		}
	}
}
