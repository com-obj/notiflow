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

package com.obj.nc.repositories;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.domain.notifIntent.NotificationIntentPersistentState;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest(properties = {
	"nc.contacts-store.jsonStorePathAndFileName=src/test/resources/contact-store/contact-store.json", 
})
public class NotificationIntentRepositoryTest extends BaseIntegrationTest {
	
	@Autowired NotificationIntentRepository intentRepository;
	@Autowired GenericEventRepository eventRepo;
	
	@BeforeEach
	void setUp(@Autowired JdbcTemplate jdbcTemplate) {
		purgeNotifTables(jdbcTemplate);
	}
	
	@Test
	public void testPersistingSingleIntent() {
		//GIVEN		
		NotificationIntent notificationIntent = NotificationIntent.createWithStaticContent(
			"Business Intelligence (BI) Developer", 
			"We are looking for a Business Intelligence"
		);   
		 
		notificationIntent.getHeader().setFlowId("default-flow");
		
		GenericEvent event = GenericEventRepositoryTest.createProcessedEvent();
		GenericEvent event2 = GenericEventRepositoryTest.createProcessedEvent();
		UUID[] eventIds = new UUID[]{
				eventRepo.save(event).getId(), 
				eventRepo.save(event2).getId()};
		notificationIntent.setPreviousEventIds(Arrays.asList(eventIds));	     
	
		NotificationIntentPersistentState notificationIntentPS = intentRepository.save(notificationIntent.toPersistentState());
		
		Optional<NotificationIntentPersistentState> oIntentInDB = intentRepository.findById(notificationIntentPS.getId());
		
		Assertions.assertThat(oIntentInDB.isPresent()).isTrue();
		NotificationIntent intentInDB = oIntentInDB.get().toIntent();
		Assertions.assertThat(intentInDB.getTimeCreated()).isNotNull();
		Assertions.assertThat(intentInDB.getHeader().getFlowId()).isEqualTo("default-flow");
		Assertions.assertThat(intentInDB.getPreviousEventIds()).isEqualTo(Arrays.asList(eventIds));
		Assertions.assertThat(intentInDB.getBody().getSubject()).contains("Business Intelligence (BI) Developer");
		Assertions.assertThat(intentInDB.getBody().getBody()).contains("We are looking for a Business Intelligence");	    
	}
	
	@Test
	public void testFindByIdInContainingIntentsId() {
		//GIVEN		
		NotificationIntent notificationIntent = NotificationIntent.createWithStaticContent(
			"Business Intelligence (BI) Developer", 
			"We are looking for a Business Intelligence"
		);   		
		notificationIntent.getHeader().setFlowId("default-flow");
		notificationIntent.setId(UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981b"));

		// WHEN
		intentRepository.save(notificationIntent.toPersistentState());

		List<NotificationIntentPersistentState> oIntentInDB = intentRepository.findByIdIn(Arrays.asList(
				UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981a"),
				UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981b")));

		// THEN
		Assertions.assertThat(!oIntentInDB.isEmpty()).isTrue();
	}
	
	@Test
	public void testFindByIdInNotContainingIntentsId() {
		//GIVEN		
		NotificationIntent notificationIntent = NotificationIntent.createWithStaticContent(
			"Business Intelligence (BI) Developer", 
			"We are looking for a Business Intelligence"
		);   
		notificationIntent.getHeader().setFlowId("default-flow");
		notificationIntent.setId(UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981b"));

		// WHEN
		List<NotificationIntentPersistentState> oIntentInDB = intentRepository.findByIdIn(Arrays.asList(
				UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981c")));

		// THEN
		Assertions.assertThat(oIntentInDB.isEmpty()).isTrue();
	}
	
	@Test
	public void testIntentReferencingNonExistingEnpoindAndEventFails() {
		//GIVEN				
		//Intent nuklada zatial do db endpointIds,.. ked sa prida toto odkomentovat
//		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);			
//		notificationIntent.setId(UUID.randomUUID());
//		notificationIntent.addReceivingEndpoints(EmailEndpoint.builder().email("test@test.sk").build());
//		
//		// WHEN
//		Assertions.assertThatThrownBy(
//				() -> intentRepository.save(notificationIntent))
//			.isInstanceOf(RuntimeException.class)
//			.hasMessageContaining("which cannot be found in the DB")
//			.hasMessageContaining("endpointIds");
		
		//GIVEN
		NotificationIntent notificationIntent = NotificationIntent.createWithStaticContent(
			"Business Intelligence (BI) Developer", 
			"We are looking for a Business Intelligence"
		);  
		notificationIntent.setId(UUID.randomUUID());
		notificationIntent.addPreviousEventId(UUID.randomUUID());
		
		// WHEN
		Assertions.assertThatThrownBy(
				() -> intentRepository.save(notificationIntent.toPersistentState()))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("which cannot be found in the DB")
			.hasMessageContaining("previousEventIds");
	}
	
}
