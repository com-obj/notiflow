package com.obj.nc.functions.processors.koderia;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Group;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.domain.event.Event;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@Log4j2
public class RecepientsUsingKoderiaSubscriptionExecution implements Function<Event, Event> {

	@DocumentProcessingInfo("FindRecepientsUsingKoderiaSubsription")
	@Override
	public Event apply(Event event) {
		// find recipients based on technologies
		Person person1 = new Person("John Doe");
		Person person2 = new Person("John Dudly");
		Person person3 = new Person("Jonson and johnson");
		Group allObjectifyGroup = Group.createWithMembers("All Objectify", person1, person2, person3);

		EmailEndpoint endpoint1 = EmailEndpoint.createForPerson(person1, "john.doe@objectify.sk");
		EmailEndpoint endpoint2 = EmailEndpoint.createForPerson(person2, "john.dudly@objectify.sk");
		EmailEndpoint endpoint3 = EmailEndpoint.createForGroup(allObjectifyGroup, "all@objectify.sk");

		DeliveryOptions deliveryOptions = new DeliveryOptions();
		deliveryOptions.setAggregationType(DeliveryOptions.AGGREGATION_TYPE.ONCE_A_WEEK);
		deliveryOptions.setSchedulingType(DeliveryOptions.TIME_CONSTRAINT_TYPE.NOT_BEFORE);
		deliveryOptions.setSchedulingValue("18:00");

		endpoint2.setDeliveryOptions(deliveryOptions);

		event.getBody().addAllRecievingEndpoints(endpoint1, endpoint2, endpoint3);

		return event;
	}

}