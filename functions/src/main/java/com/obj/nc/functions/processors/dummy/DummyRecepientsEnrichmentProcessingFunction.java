package com.obj.nc.functions.processors.dummy;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Group;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
@DocumentProcessingInfo("DummyRecepientsEnrichment")
public class DummyRecepientsEnrichmentProcessingFunction extends ProcessorFunctionAdapter<NotificationIntent<?>, NotificationIntent<?>> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(NotificationIntent<?> notificationIntent) {
		
		return Optional.empty();
	}

	@Override
	protected NotificationIntent<?> execute(NotificationIntent<?> notificationIntent) {
		// find recipients based on technologies
		Person person1 = new Person("John Doe");
		Person person2 = new Person("John Dudly");
		Person person3 = new Person("Jonson and johnson");
		Group allObjectifyGroup = Group.createWithMembers("All Objectify", person1, person2, person3);

		EmailEndpoint endpoint1 = EmailEndpoint.createForPerson(person1, "john.doe@objectify.sk");
		EmailEndpoint endpoint2 = EmailEndpoint.createForPerson(person2, "john.dudly@objectify.sk");
		EmailEndpoint endpoint3 = EmailEndpoint.createForGroup(allObjectifyGroup, "all@objectify.sk");

		notificationIntent.addRecievingEndpoints(endpoint1, endpoint2, endpoint3);

		return notificationIntent;
	}

}
