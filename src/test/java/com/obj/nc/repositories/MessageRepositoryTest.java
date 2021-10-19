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
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistentState;
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
@SpringBootTest
public class MessageRepositoryTest extends BaseIntegrationTest {
	
	@Autowired MessageRepository messageRepository;
	@Autowired EndpointsRepository endpointRepo;
	@Autowired GenericEventRepository eventRepo;
	
	@BeforeEach
	void setUp(@Autowired JdbcTemplate jdbcTemplate) {
		purgeNotifTables(jdbcTemplate);
	}
	
	@Test
	public void testPersistingSingleMessage() {
		//GIVEN
		EmailMessage emailMsg = createTestMessage();	
		
		emailMsg.getHeader().setFlowId("default-flow");
		
		GenericEvent event = GenericEventRepositoryTest.createDirectMessageEvent();
		GenericEvent event2 = GenericEventRepositoryTest.createDirectMessageEvent();
		UUID[] eventIds = new UUID[]{
				eventRepo.save(event).getId(), 
				eventRepo.save(event2).getId()};
		emailMsg.setPreviousEventIds(Arrays.asList(eventIds));
		
		messageRepository.save(emailMsg.toPersistentState());
		
		Optional<MessagePersistentState> oEmailInDB = messageRepository.findById(emailMsg.getId());
		
		Assertions.assertThat(oEmailInDB.isPresent()).isTrue();
		EmailMessage emailInDB = oEmailInDB.get().toMessage();
		Assertions.assertThat(emailInDB.getPayloadTypeName()).isEqualTo("EMAIL_MESSAGE"); 
		Assertions.assertThat(emailInDB.getTimeCreated()).isNotNull();
		Assertions.assertThat(emailInDB.getHeader().getFlowId()).isEqualTo("default-flow");
		Assertions.assertThat(emailInDB.getPreviousEventIds()).isEqualTo(Arrays.asList(eventIds));
	}

	
	@Test
	public void testFindByIdInContainingIntentsId() {
		EmailMessage emailMsg = createTestMessage();		

		emailMsg.setId(UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981b"));
		messageRepository.save(emailMsg.toPersistentState());
		// WHEN
		List<MessagePersistentState> oIntentInDB = messageRepository.findByIdIn(Arrays.asList(
				UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981a"),
				UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981b")));
		// THEN
		Assertions.assertThat(oIntentInDB.size()).isEqualTo(1);
	}
	
	@Test
	public void testFindByIdInNotContainingIntentsId() {
		EmailMessage emailMsg = createTestMessage();		
		
		emailMsg.setId(UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981b"));
		messageRepository.save(emailMsg.toPersistentState());
		// WHEN
		List<MessagePersistentState> oIntentInDB = messageRepository.findByIdIn(Arrays.asList(
				UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981c")));
		// THEN
		Assertions.assertThat(oIntentInDB.isEmpty()).isTrue();
	}
	
	@Test
	public void testMessageReferencingNonExistingEnpoindFails() {
		//GIVEN
		String INPUT_JSON_FILE = "messages/email_message.json";
		final EmailMessage emailMsg = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessage.class);				
		emailMsg.setId(UUID.randomUUID());
		emailMsg.getPreviousEventIds().clear();
		
		// WHEN
		Assertions.assertThatThrownBy(
				() -> messageRepository.save(emailMsg.toPersistentState()))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("which cannot be found in the DB")
			.hasMessageContaining("endpointIds");
		
		//GIVEN
		INPUT_JSON_FILE = "messages/email_message.json";
		final EmailMessage emailMsg2 = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessage.class);				
		emailMsg2.setId(UUID.randomUUID());
		emailMsg2.addPreviousEventId(UUID.randomUUID());
		emailMsg2.getReceivingEndpoints().forEach(endpoint -> endpointRepo.persistEnpointIfNotExists(endpoint));
		
		// WHEN
		Assertions.assertThatThrownBy(
				() -> messageRepository.save(emailMsg2.toPersistentState()))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("which cannot be found in the DB")
			.hasMessageContaining("previousEventIds");
	}
	

	private EmailMessage createTestMessage() {
		String INPUT_JSON_FILE = "messages/email_message.json";
		EmailMessage emailMsg = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessage.class);
		emailMsg.ensureEndpointsPersisted();
		
		return emailMsg;
	}
	
}
