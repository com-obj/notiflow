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

import com.obj.nc.Get;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
public class GenericEventRepositoryTest extends BaseIntegrationTest {

	@Autowired GenericEventRepository eventRepository;
	@Autowired MessageRepository messageRepository;
	@Autowired EndpointsRepository endpointRepo;


	@BeforeEach
	void setUp(@Autowired JdbcTemplate jdbcTemplate) {
		purgeNotifTables(jdbcTemplate);
	}

	@Test
	public void testPersistingSingleEvent() {
		//GIVEN
		GenericEvent event = createProcessedEvent();
		
		eventRepository.save(event);
		
		Optional<GenericEvent> savedEvent = eventRepository.findById(event.getId());
		
		Assertions.assertThat(savedEvent.isPresent()).isTrue();

	}

	@Test
	public void testShouldFindOneEventForSummaryNotification() {
		persistEventWithDeliveryInfoProcessedOneDayAgo();

		Instant now = LocalDateTime.now().toInstant(ZoneOffset.ofTotalSeconds(0));
		Instant period = now.minus(1, ChronoUnit.DAYS);
		List<GenericEvent> events = eventRepository.findEventsForSummaryNotification(period);
		Assertions.assertThat(events).hasSize(1);
	}

	@Test
	public void testShouldNotFindAnyEventForSummaryNotification() {
		persistEventWithDeliveryInfoProcessedOneDayAgo();

		Instant now = LocalDateTime.now().toInstant(ZoneOffset.ofTotalSeconds(0));
		Instant period = now.minus(2, ChronoUnit.DAYS);

		List<GenericEvent> events = eventRepository.findEventsForSummaryNotification(period);
		Assertions.assertThat(events).isEmpty();
	}

	private void persistEventWithDeliveryInfoProcessedOneDayAgo() {
		GenericEvent event = createProcessedEvent();
		event.setNotifyAfterProcessing(true);
		eventRepository.save(event);

		EmailMessage message = createTestMessage();
		message.addPreviousEventId(event.getId());
		messageRepository.save(message.toPersistentState());

		List<EmailEndpoint> endpoint = endpointRepo.persistEnpointIfNotExists(message.getReceivingEndpoints());

		DeliveryInfo deliveryInfo = DeliveryInfo.builder()
				.endpointId(endpoint.get(0).getId())
				.messageId(message.getId())
				.status(DeliveryInfo.DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		deliveryInfoRepo.save(deliveryInfo);

		Get.getJdbc().update("update nc_delivery_info set processed_on = now() - INTERVAL '30 hours'");
	}

	public static GenericEvent createProcessedEvent() {
		String content = JsonUtils.readJsonStringFromClassPathResource("intents/direct_message.json");
		
		GenericEvent event = GenericEvent.builder()
				.externalId(UUID.randomUUID().toString())
				.flowId("FLOW_ID")
				.id(UUID.randomUUID())
				.payloadJson(JsonUtils.readJsonNodeFromJSONString(content))
				.timeConsumed(Instant.now())
				.build();
		return event;
	}

	private EmailMessage createTestMessage() {
		String INPUT_JSON_FILE = "messages/email_message.json";
		EmailMessage emailMsg = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessage.class);
		return emailMsg;
	}

}
