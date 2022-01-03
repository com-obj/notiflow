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

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
public class DeliveryInfoRepositoryTest extends BaseIntegrationTest {

	@Autowired DeliveryInfoRepository deliveryInfoRepo;
	@Autowired GenericEventRepository eventRepo;
	@Autowired EndpointsRepository endpointRepo;
	@Autowired MessageRepository messageRepo;
	
	@BeforeEach
	public void clean() {
		deliveryInfoRepo.deleteAll();
	}
	
	@Test
	public void testPersistingSingleInfo() {
		//GIVEN
		GenericEvent event = GenericEventRepositoryTest.createProcessedEvent();
		eventRepo.save(event);
		
		EmailEndpoint email = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
		UUID endpointId = endpointRepo.persistEnpointIfNotExists(email).getId();
		
		//WHEN
		DeliveryInfo deliveryInfo = DeliveryInfo.builder()
				.endpointId(endpointId)
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		deliveryInfoRepo.save(deliveryInfo);
		
		//AND WHEN
		Optional<DeliveryInfo> infoInDb = deliveryInfoRepo.findById(deliveryInfo.getId());
		
		//THEN
		Assertions.assertThat(infoInDb.isPresent()).isTrue();
	}
	
	@Test
	public void testFindByEventId() {
		//GIVEN
		GenericEvent event = GenericEventRepositoryTest.createProcessedEvent();
		UUID eventId = eventRepo.save(event).getId();
		
		GenericEvent event2 = GenericEventRepositoryTest.createProcessedEvent();
		UUID eventId2 = eventRepo.save(event2).getId();
		
		EmailEndpoint email1 = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
		UUID endpointId = endpointRepo.persistEnpointIfNotExists(email1).getId();

		EmailMessage message1 = new EmailMessage();
		message1.addPreviousEventId(eventId);
		message1 = messageRepo.save(message1.toPersistentState()).toMessage();

		EmailMessage message2 = new EmailMessage();
		message2.addPreviousEventId(eventId2);
		message2 = messageRepo.save(message2.toPersistentState()).toMessage();

		//AND GIVEN
		DeliveryInfo deliveryInfo1 = DeliveryInfo.builder()
				.endpointId(endpointId)
				.messageId(message1.getId())
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		DeliveryInfo deliveryInfo2 = DeliveryInfo.builder()
				.endpointId(endpointId)
				.messageId(message2.getId())
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		deliveryInfoRepo.save(deliveryInfo1);
		deliveryInfoRepo.save(deliveryInfo2);
		
		List<DeliveryInfo> infosInDb = deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId);
		
		Assertions.assertThat(infosInDb.size()).isEqualTo(1);
	}
	
	@Test
	public void testFindByEventIdAndEndpointId() {
		//GIVEN
		GenericEvent event = GenericEventRepositoryTest.createProcessedEvent();
		UUID eventId = eventRepo.save(event).getId();
		
		GenericEvent event2 = GenericEventRepositoryTest.createProcessedEvent();
		UUID eventId2 = eventRepo.save(event2).getId();
		
		EmailEndpoint email1 = EmailEndpoint.builder().email("johndoe@gmail.com").build();
		UUID endpointId1 = endpointRepo.persistEnpointIfNotExists(email1).getId();
		
		EmailEndpoint email2 = EmailEndpoint.builder().email("johndudly@gmail.com").build();
		UUID endpointId2 = endpointRepo.persistEnpointIfNotExists(email2).getId();

		EmailMessage message1 = new EmailMessage();
		message1.addPreviousEventId(eventId);
		message1 = messageRepo.save(message1.toPersistentState()).toMessage();

		EmailMessage message2 = new EmailMessage();
		message2.addPreviousEventId(eventId2);
		message2 = messageRepo.save(message2.toPersistentState()).toMessage();

		//AND GIVEN
		DeliveryInfo deliveryInfo1 = DeliveryInfo.builder()
				.endpointId(endpointId1)
				.messageId(message1.getId())
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		DeliveryInfo deliveryInfo2 = DeliveryInfo.builder()
				.endpointId(endpointId2)
				.messageId(message1.getId())
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		DeliveryInfo deliveryInfo3 = DeliveryInfo.builder()
				.endpointId(endpointId2)
				.messageId(message2.getId())
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		deliveryInfoRepo.save(deliveryInfo1);
		deliveryInfoRepo.save(deliveryInfo2);
		deliveryInfoRepo.save(deliveryInfo3);
		
		List<DeliveryInfo> infosInDb = deliveryInfoRepo.findByEventIdAndEndpointIdOrderByProcessedOn(eventId, endpointId1);
		
		Assertions.assertThat(infosInDb.size()).isEqualTo(1);
	}
	
	@Test
	public void testFindByEndpointId() {
		//GIVEN
		GenericEvent event = GenericEventRepositoryTest.createProcessedEvent();
		UUID eventId = eventRepo.save(event).getId();
		
		EmailEndpoint email1 = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
		UUID endpointId = endpointRepo.persistEnpointIfNotExists(email1).getId();
		
		EmailEndpoint email2 = EmailEndpoint.builder().email("jancuzy2@gmail.com").build();
		UUID endpoint2Id = endpointRepo.persistEnpointIfNotExists(email2).getId();
		
		// AND GIVEN
		DeliveryInfo deliveryInfo1 = DeliveryInfo.builder()
				.endpointId(endpointId)
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		DeliveryInfo deliveryInfo2 = DeliveryInfo.builder()
				.endpointId(endpointId)
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		DeliveryInfo deliveryInfo3 = DeliveryInfo.builder()
				.endpointId(endpoint2Id)
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		//WHEN
		deliveryInfoRepo.save(deliveryInfo1);
		deliveryInfoRepo.save(deliveryInfo2);
		deliveryInfoRepo.save(deliveryInfo3);
		
		//THEN
		List<DeliveryInfo> infosInDb = deliveryInfoRepo.findByEndpointIdOrderByProcessedOn(endpointId);
		
		Assertions.assertThat(infosInDb.size()).isEqualTo(2);
	}
	
	@Test
	public void testSaveWrongReferences() {
		//GIVEN
		DeliveryInfo deliveryInfo1 = DeliveryInfo.builder()
				.endpointId(UUID.randomUUID())
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		// WHEN
		Assertions.assertThatThrownBy(
				() -> deliveryInfoRepo.save(deliveryInfo1))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("which cannot be found in the DB")
			.hasMessageContaining("endpointId");

		//GIVEN
		DeliveryInfo deliveryInfo3 = DeliveryInfo.builder()
				.messageId(UUID.randomUUID())
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		// WHEN
		Assertions.assertThatThrownBy(
				() -> deliveryInfoRepo.save(deliveryInfo3))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("which cannot be found in the DB")
			.hasMessageContaining("messageId");
	}

}
