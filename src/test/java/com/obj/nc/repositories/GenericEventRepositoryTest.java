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
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
public class GenericEventRepositoryTest {

	@Autowired GenericEventRepository eventRepository;
	
	@Test
	public void testPersistingSingleEvent() {
		//GIVEN
		GenericEvent event = createDirectMessageEvent();
		
		eventRepository.save(event);
		
		Optional<GenericEvent> savedEvent = eventRepository.findById(event.getId());
		
		Assertions.assertThat(savedEvent.isPresent()).isTrue();

	}

	public static GenericEvent createDirectMessageEvent() {
		String INPUT_JSON_FILE = "intents/direct_message.json";
		String content = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);
		
		GenericEvent event = GenericEvent.builder()
				.externalId(UUID.randomUUID().toString())
				.flowId("FLOW_ID")
				.id(UUID.randomUUID())
				.payloadJson(JsonUtils.readJsonNodeFromJSONString(content))
				.timeConsumed(Instant.now())
				.build();
		return event;
	}
}
