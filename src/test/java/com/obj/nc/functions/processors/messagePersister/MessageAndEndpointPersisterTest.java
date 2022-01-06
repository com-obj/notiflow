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

package com.obj.nc.functions.processors.messagePersister;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

import java.util.Arrays;
import java.util.Optional;

import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.repositories.MessageRepository;
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
@SpringBootTest
public class MessageAndEndpointPersisterTest extends BaseIntegrationTest {

	@Autowired MessageAndEndpointPersister messageAndEndpointPersister;
	@Autowired MessageRepository messageRepo;	

	@BeforeEach
	void setUp(@Autowired JdbcTemplate jdbcTemplate) {
		purgeNotifTables(jdbcTemplate);
	}

	@Test
	public void testPersistingSingleMessage() {
		//GIVEN
		EmailMessage emailMessage = new EmailMessage();
		emailMessage.setBody(EmailContent.builder().subject("Subject").text("Text").build());
		emailMessage.setReceivingEndpoints(Arrays.asList(
				EmailEndpoint.builder().email("john.doe@gmail.com").build(),
				EmailEndpoint.builder().email("john.dudly@gmail.com").build()));

		Message<?> emailMessageInDb = messageAndEndpointPersister.apply(emailMessage);

		Optional<MessagePersistentState> oEmailInDB = messageRepo.findById(emailMessageInDb.getId());

		Assertions.assertThat(oEmailInDB.isPresent()).isTrue();
		EmailMessage emailInDB = oEmailInDB.get().toMessage();
		Assertions.assertThat(emailInDB.getPayloadTypeName()).isEqualTo("EMAIL_MESSAGE");
		Assertions.assertThat(emailInDB.getTimeCreated()).isNotNull();
		Assertions.assertThat(emailInDB.getReceivingEndpoints().size()).isEqualTo(2);
	}



}
