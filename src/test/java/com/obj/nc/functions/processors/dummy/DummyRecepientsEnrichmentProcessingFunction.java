/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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

@Component
@AllArgsConstructor
@DocumentProcessingInfo("DummyRecipientsEnrichment")
public class DummyRecepientsEnrichmentProcessingFunction extends ProcessorFunctionAdapter<NotificationIntent, NotificationIntent> {

	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(NotificationIntent notificationIntent) {
		return Optional.empty();
	}

	@Override
	protected NotificationIntent execute(NotificationIntent notificationIntent) {
		// find recipients based on technologies
		Person person1 = new Person("John Doe");
		Person person2 = new Person("John Dudly");
		Person person3 = new Person("Jonson and johnson");
		Group allObjectifyGroup = Group.createWithMembers("All Objectify", person1, person2, person3);

		EmailEndpoint endpoint1 = EmailEndpoint.createForPerson(person1, "john.doe@objectify.sk");
		EmailEndpoint endpoint2 = EmailEndpoint.createForPerson(person2, "john.dudly@objectify.sk");
		EmailEndpoint endpoint3 = EmailEndpoint.createForGroup(allObjectifyGroup, "all@objectify.sk");

		notificationIntent.addReceivingEndpoints(endpoint1, endpoint2, endpoint3);

		return notificationIntent;
	}

}
