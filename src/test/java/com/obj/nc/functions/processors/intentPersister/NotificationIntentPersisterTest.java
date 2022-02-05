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

package com.obj.nc.functions.processors.intentPersister;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.domain.notifIntent.NotificationIntentPersistentState;
import com.obj.nc.domain.recipients.Group;
import com.obj.nc.domain.recipients.Person;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.GenericEventRepositoryTest;
import com.obj.nc.repositories.NotificationIntentRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest(properties = {
    "nc.contacts-store.jsonStorePathAndFileName=src/test/resources/contact-store/contact-store.json", 
})
public class NotificationIntentPersisterTest extends BaseIntegrationTest {

	@Autowired NotificationIntentPersister intentPersister;
	@Autowired NotificationIntentRepository intentRepo;	
	@Autowired GenericEventRepository eventRepository;

	@BeforeEach
	void setUp(@Autowired JdbcTemplate jdbcTemplate) {
		purgeNotifTables(jdbcTemplate);
	}

	@Test
	public void testPersistingSingleMessage() {
		//GIVEN
		GenericEvent event = GenericEventRepositoryTest.createProcessedEvent();
		UUID eventId = eventRepository.save(event).getId();
		
		NotificationIntent notificationIntent = NotificationIntent.createWithStaticContent(
			"Business Intelligence (BI) Developer", 
			"We are looking for a Business Intelligence"
		);  	
        notificationIntent.addPreviousEventId(eventId);
		//as defined in contact store
		UUID johnDoeId = UUID.fromString("baf25cbe-2975-4666-adda-c8ea01dc909d");	
		UUID objectifyId = UUID.fromString("ce48aab7-7160-4b1d-b22c-0bd1bb8bbce2");
		notificationIntent.addRecipients(
			Person.builder().id(johnDoeId).build(),
			Group.builder().id(objectifyId).build()
		);

		NotificationIntent saveIntent = intentPersister.apply(notificationIntent);

		Assertions.assertThat(saveIntent.getPreviousEventIds()).isEqualTo(notificationIntent.getPreviousEventIds());
		Assertions.assertThat(saveIntent.getBody().getSubject()).isEqualTo(notificationIntent.getBody().getSubject());
		Assertions.assertThat(saveIntent.getBody().getBody()).isEqualTo(notificationIntent.getBody().getBody());
		Assertions.assertThat(saveIntent.getRecipients()).isEqualTo(notificationIntent.getRecipients());

		Optional<NotificationIntentPersistentState> oIntentInDB = intentRepo.findById(saveIntent.getId());

		Assertions.assertThat(oIntentInDB.isPresent()).isTrue();
		NotificationIntent intentInDB = oIntentInDB.get().toIntent();
		Assertions.assertThat(intentInDB.getTimeCreated()).isNotNull();
		Assertions.assertThat(intentInDB.getPreviousEventIds()).isEqualTo(notificationIntent.getPreviousEventIds());
		Assertions.assertThat(intentInDB.getBody().getSubject()).isEqualTo(notificationIntent.getBody().getSubject());
		Assertions.assertThat(intentInDB.getBody().getBody()).isEqualTo(notificationIntent.getBody().getBody());
		Assertions.assertThat(intentInDB.getRecipients()).isEqualTo(notificationIntent.getRecipients());
	}



}
