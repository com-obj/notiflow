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

package com.obj.nc.controllers;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.jayway.jsonpath.JsonPath;
import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.controllers.DeliveryInfoRestController.EndpointDeliveryInfoDto;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.dto.endpoint.EmailEndpointDto;
import com.obj.nc.domain.dto.endpoint.SmsEndpointDto;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.domain.pagination.ResultPage;
import com.obj.nc.flows.messageProcessing.MessageProcessingFlow;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.*;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.DateFormatMatcher;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.READ;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.SENT;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class DeliveryInfoControllerTest extends BaseIntegrationTest {


	@Autowired
	private DeliveryInfoRepository deliveryRepo;
	@Autowired
	private MessageRepository messageRepo;
	@Autowired
	private EndpointsRepository endpointRepo;
	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	private DeliveryInfoRestController controller;
	@Autowired
	GenericEventRepository eventRepo;
	@Autowired
	private MessageProcessingFlow messageProcessingFlow;
	@Autowired
	private NcAppConfigProperties ncAppConfigProperties;

	@RegisterExtension
	protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
			.withConfiguration(
					GreenMailConfiguration.aConfig()
							.withUser("no-reply@objectify.sk", "xxx"))
			.withPerMethodLifecycle(true);

	@BeforeEach
	void setUp(@Autowired JdbcTemplate jdbcTemplate) {
		purgeNotifTables(jdbcTemplate);
	}

	@Test
	void testFindEventDeliveryInfos() throws Exception {
		//GIVEN
		EmailEndpoint email1 = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
		SmsEndpoint sms1 = SmsEndpoint.builder().phone("0908111111").build();
		Map<String, ReceivingEndpoint> endpoints = endpointRepo.persistEnpointIfNotExistsMappedToNameId(email1, sms1);
		UUID emailEndPointId = endpoints.get("jancuzy@gmail.com").getId();
		UUID smsEndPointId = endpoints.get("0908111111").getId();

		//AND
		GenericEvent event = GenericEventRepositoryTest.createProcessedEvent();
		UUID eventId = eventRepo.save(event).getId();

		List<UUID> eventIds = Collections.singletonList(eventId);

		EmailMessage emailMessage = new EmailMessage();
		emailMessage.setPreviousEventIds(eventIds);
		emailMessage = messageRepo.save(emailMessage.toPersistentState()).toMessage();

		SmsMessage smsMessage = new SmsMessage();
		smsMessage.setPreviousEventIds(eventIds);
		smsMessage.setBody(new SimpleTextContent("Something terrible happened!"));
		smsMessage = messageRepo.save(smsMessage.toPersistentState()).toMessage();

		//AND
		DeliveryInfo info1 = DeliveryInfo.builder()
				.endpointId(emailEndPointId).messageId(emailMessage.getId()).status(DELIVERY_STATUS.PROCESSING).id(UUID.randomUUID()).build();
		DeliveryInfo info2 = DeliveryInfo.builder()
				.endpointId(emailEndPointId).messageId(emailMessage.getId()).status(SENT).id(UUID.randomUUID()).build();


		DeliveryInfo info3 = DeliveryInfo.builder()
				.endpointId(smsEndPointId).messageId(smsMessage.getId()).status(DELIVERY_STATUS.PROCESSING).id(UUID.randomUUID()).build();
		Thread.sleep(10); // to have different processedOn

		DeliveryInfo info4 = DeliveryInfo.builder()
				.endpointId(smsEndPointId).messageId(smsMessage.getId()).status(SENT).id(UUID.randomUUID()).build();

		deliveryRepo.saveAll(Arrays.asList(info1, info2, info3, info4));

		//WHEN
		ResultPage<EndpointDeliveryInfoDto> infos = controller.findDeliveryInfosByEventId(eventId.toString(), null, PageRequest.of(0, 20));

		//THEN
		Assertions.assertThat(infos.getContent().size()).isEqualTo(2);
		EndpointDeliveryInfoDto infoForEmail = infos.getContent().stream().filter(i -> i.getEndpoint() instanceof EmailEndpointDto).findFirst().get();

		Instant now = Instant.now();

		Assertions.assertThat(infoForEmail.getEndpoint().getId()).isEqualTo(emailEndPointId.toString());
		Assertions.assertThat(infoForEmail.getStatusReachedAt()).isCloseTo(now, Assertions.within(1, ChronoUnit.MINUTES));
		Assertions.assertThat(infoForEmail.getCurrentStatus()).isEqualTo(SENT);

		EndpointDeliveryInfoDto infoForSms = infos.getContent().stream().filter(i -> i.getEndpoint() instanceof SmsEndpointDto).findFirst().get();

		Assertions.assertThat(infoForSms.getEndpoint().getId()).isEqualTo(smsEndPointId.toString());
		Assertions.assertThat(infoForSms.getStatusReachedAt()).isCloseTo(now, Assertions.within(1, ChronoUnit.MINUTES));
		Assertions.assertThat(infoForSms.getCurrentStatus()).isEqualTo(SENT);

	}

	@Test
	void testFindEventDeliveryInfosRest() throws Exception {
		//GIVEN
		EmailEndpoint email1 = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
		UUID emailEndpointId = endpointRepo.persistEnpointIfNotExists(email1).getId();

		//AND
		GenericEvent event = GenericEventRepositoryTest.createProcessedEvent();
		UUID eventId = eventRepo.save(event).getId();

		EmailMessage message = new EmailMessage();
		message.setPreviousEventIds(Collections.singletonList(eventId));
		message = messageRepo.save(message.toPersistentState()).toMessage();

		//AND
		DeliveryInfo info1 = DeliveryInfo.builder()
				.endpointId(emailEndpointId).messageId(message.getId()).status(DELIVERY_STATUS.PROCESSING).id(UUID.randomUUID()).build();
		DeliveryInfo info2 = DeliveryInfo.builder()
				.endpointId(emailEndpointId).messageId(message.getId()).status(SENT).id(UUID.randomUUID()).build();

		deliveryRepo.saveAll(Arrays.asList(info1, info2));

		//WHEN TEST REST
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/delivery-info/events/{eventId}", eventId.toString())
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());

		//THEN

		resp
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.content.[0].currentStatus").value(CoreMatchers.is("SENT")))
				.andExpect(jsonPath("$.content.[0].statusReachedAt").value(DateFormatMatcher.matchesISO8601()))
				.andExpect(jsonPath("$.content.[0].endpoint.email").value(CoreMatchers.is("jancuzy@gmail.com")))
				.andExpect(jsonPath("$.content.[0].endpoint.value").value(CoreMatchers.is("jancuzy@gmail.com")))
				.andExpect(jsonPath("$.content.[0].endpoint.type").value(CoreMatchers.is("EMAIL"))).andReturn();
	}

	@Test
	void testFindMessageDeliveryInfos() {
		// GIVEN
		EmailEndpoint email1 = EmailEndpoint.builder().email("john.doe@gmail.com").build();
		email1 = endpointRepo.persistEnpointIfNotExists(email1);
		EmailEndpoint email2 = EmailEndpoint.builder().email("john.dudly@gmail.com").build();
		email2 = endpointRepo.persistEnpointIfNotExists(email2);

		//AND
		EmailMessage emailMessage = new EmailMessage();
		emailMessage.setBody(EmailContent.builder().subject("Subject").text("Text").build());
		emailMessage.setReceivingEndpoints(Arrays.asList(email1, email2));

		messageProcessingFlow.processMessage(emailMessage);
		Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> deliveryRepo.countByMessageIdAndStatus(emailMessage.getId(), SENT) >= 2);

		// WHEN find infos by input message's id
		List<EndpointDeliveryInfoDto> deliveryInfosByMessageId = controller.findDeliveryInfosByMessageId(emailMessage.getId().toString());

		// THEN should find latest states of tokenized messages
		assertThat(deliveryInfosByMessageId)
				.hasSize(2)
				.allMatch(endpointDeliveryInfoDto -> SENT.equals(endpointDeliveryInfoDto.getCurrentStatus()));

		assertThat(deliveryInfosByMessageId)
				.filteredOn(endpointDeliveryInfoDto -> "john.doe@gmail.com".equals(endpointDeliveryInfoDto.getEndpoint().getValue()))
				.isNotEmpty();

		assertThat(deliveryInfosByMessageId)
				.filteredOn(endpointDeliveryInfoDto -> "john.dudly@gmail.com".equals(endpointDeliveryInfoDto.getEndpoint().getValue()))
				.isNotEmpty();
	}

	@Test
	void testMarkAsReadMessageDeliveryInfo() throws Exception {
		//GIVEN
		EmailEndpoint email1 = EmailEndpoint.builder().email("john.doe@gmail.com").build();
		email1 = endpointRepo.persistEnpointIfNotExists(email1);
		EmailEndpoint email2 = EmailEndpoint.builder().email("john.dudly@gmail.com").build();
		email2 = endpointRepo.persistEnpointIfNotExists(email2);

		//AND
		EmailMessage emailMessage = new EmailMessage();
		emailMessage.setBody(EmailContent.builder().subject("Subject").text("Text").build());
		emailMessage.setReceivingEndpoints(Arrays.asList(email1, email2));

		messageProcessingFlow.processMessage(emailMessage);
		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> deliveryRepo.countByMessageIdAndStatus(emailMessage.getId(), SENT) >= 2);

		List<MessagePersistentState> messages = StreamSupport.stream(messageRepo.findAll().spliterator(), false)
				.filter(message -> !emailMessage.getId().equals(message.getId()))
				.collect(toList());

		//WHEN TEST REST
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders
						.get(ncAppConfigProperties.getContextPath() + "/delivery-info/messages/{messageId}/mark-as-read", Objects.requireNonNull(messages.get(0).getId()).toString())
						.contextPath(ncAppConfigProperties.getContextPath())
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());

		//THEN REDIRECT TO IMAGE
		resp
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(ncAppConfigProperties.getContextPath() + "/resources/images/px.png"));

		//AND IMAGE IS FOUND
		mockMvc.perform(MockMvcRequestBuilders
						.get(ncAppConfigProperties.getContextPath() + "/resources/images/px.png")
						.contextPath(ncAppConfigProperties.getContextPath()))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().contentType(MediaType.IMAGE_PNG));

		//AND READ STATUS IS JOURNALIZED
		Awaitility.await()
				.atMost(5, TimeUnit.SECONDS)
				.until(() -> deliveryRepo.countByMessageIdAndStatus(emailMessage.getId(), READ) >= 1);

		List<DeliveryInfo> infosOfMessage = deliveryRepo.findByMessageIdAndStatusOrderByProcessedOn(emailMessage.getId(), READ);
		assertThat(infosOfMessage).hasSize(1);
	}

	@Test
	void testMarkAsReadMessageDeliveryInfoNotPersistingDuplicates() throws Exception {
		//GIVEN
		EmailEndpoint email1 = EmailEndpoint.builder().email("john.doe@gmail.com").build();
		email1 = endpointRepo.persistEnpointIfNotExists(email1);
		EmailEndpoint email2 = EmailEndpoint.builder().email("john.dudly@gmail.com").build();
		email2 = endpointRepo.persistEnpointIfNotExists(email2);

		//AND
		EmailMessage emailMessage = new EmailMessage();
		emailMessage.setBody(EmailContent.builder().subject("Subject").text("Text").build());
		emailMessage.setReceivingEndpoints(Arrays.asList(email1, email2));

		messageProcessingFlow.processMessage(emailMessage);
		Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> deliveryRepo.countByMessageIdAndStatus(emailMessage.getId(), SENT) >= 2);

		List<DeliveryInfo> sentInfos = deliveryRepo.findByMessageIdAndStatusOrderByProcessedOn(emailMessage.getId(), SENT);

		//WHEN TEST REST
		ResultActions resp1 = mockMvc
				.perform(MockMvcRequestBuilders
						.get(ncAppConfigProperties.getContextPath() + "/delivery-info/messages/{messageId}/mark-as-read", Objects.requireNonNull(sentInfos.get(0).getMessageId()).toString())
						.contextPath(ncAppConfigProperties.getContextPath())
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());

		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> deliveryRepo.countByMessageIdAndStatus(sentInfos.get(0).getMessageId(), READ) >= 1);

		ResultActions resp2 = mockMvc
				.perform(MockMvcRequestBuilders
						.get(ncAppConfigProperties.getContextPath() + "/delivery-info/messages/{messageId}/mark-as-read", Objects.requireNonNull(sentInfos.get(0).getMessageId()).toString())
						.contextPath(ncAppConfigProperties.getContextPath())
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());

		Awaitility.await().atLeast(2, TimeUnit.SECONDS);

		//AND READ STATUS IS JOURNALIZED ONLY ONCE
		List<DeliveryInfo> infosOfMessage = deliveryRepo.findByMessageIdAndStatusOrderByProcessedOn(sentInfos.get(0).getMessageId(), READ);
		assertThat(infosOfMessage).hasSize(1);
	}

	@Test
	void testEventWithExtIdDoesNotExist() throws Exception {
		String nonExistingExtId = UUID.randomUUID().toString();
		mockMvc.perform(MockMvcRequestBuilders.get("/delivery-info/events/ext/{extId}", nonExistingExtId)
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andExpect(MockMvcResultMatchers.content()
						.string("Event with " + nonExistingExtId + " external ID not found"));
	}

	@Test
	void testEventHasNoMessagesAttached() throws Exception {
		GenericEvent event = GenericEventRepositoryTest.createProcessedEvent();
		String extId = eventRepo.save(event).getExternalId();

		mockMvc.perform(MockMvcRequestBuilders.get("/delivery-info/events/ext/{extId}", extId)
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andExpect(MockMvcResultMatchers.content().json("{\"content\": []}"));
	}

	@Test
	void testMatchOnDeliveryInfoWithMessage() throws Exception {
		// GIVEN
		EmailEndpoint email = endpointRepo
				.persistEnpointIfNotExists(EmailEndpoint.builder().email("kosarnik@objectify.sk").build());
		GenericEvent event = eventRepo.save(GenericEventRepositoryTest.createProcessedEvent());

		EmailMessage message = new EmailMessage();
		message.setPreviousEventIds(Collections.singletonList(event.getEventId()));
		message = messageRepo.save(message.toPersistentState()).toMessage();

		DeliveryInfo deliveryInfo = DeliveryInfo.builder()
				.endpointId(email.getId()).messageId(message.getId()).status(DELIVERY_STATUS.PROCESSING)
				.id(UUID.randomUUID()).build();

		deliveryRepo.save(deliveryInfo);

		// WHEN
		mockMvc.perform(MockMvcRequestBuilders.get("/delivery-info/events/ext/{extId}", event.getExternalId())
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.content", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.content.[0].endpoint.id", Matchers.is(email.getId().toString())))
				.andExpect(jsonPath("$.content.[0].endpoint.email", Matchers.is(email.getEmail())))
				.andExpect(jsonPath("$.content.[0].endpoint.value", Matchers.is(email.getEndpointId())))
				.andExpect(jsonPath("$.content.[0].currentStatus", Matchers.is(deliveryInfo.getStatus().name())));
	}

	@Test
	void testGetPage1Size20() throws Exception {
		String extId = setupMessageDeliveriesForSingleEvent();

		ResultActions resp = mockMvc.perform(MockMvcRequestBuilders.get("/delivery-info/events/ext/{extId}", extId)
						.param("page", "1")
						.param("size", "20")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());

		String respContentAsStr = resp.andReturn().getResponse().getContentAsString();

		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(respContentAsStr, "$.content");
		assertThat(endpoints).hasSize(15);

		boolean next = JsonPath.read(respContentAsStr, "$.next");
		assertThat(next).isFalse();
	}

	@Test
	void testGetPage1Size10() throws Exception {
		String extId = setupMessageDeliveriesForSingleEvent();

		ResultActions resp = mockMvc.perform(MockMvcRequestBuilders.get("/delivery-info/events/ext/{extId}", extId)
						.param("page", "1")
						.param("size", "10")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());

		String respContentAsStr = resp.andReturn().getResponse().getContentAsString();

		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(respContentAsStr, "$.content");
		assertThat(endpoints).hasSize(10);

		boolean next = JsonPath.read(respContentAsStr, "$.next");
		assertThat(next).isTrue();

		boolean isEmpty = JsonPath.read(respContentAsStr, "$.empty");
		assertThat(isEmpty).isFalse();
	}

	@Test
	void testGetPage2Size10() throws Exception {
		String extId = setupMessageDeliveriesForSingleEvent();

		ResultActions resp = mockMvc.perform(MockMvcRequestBuilders.get("/delivery-info/events/ext/{extId}", extId)
						.param("page", "2")
						.param("size", "10")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());

		String respContentAsStr = resp.andReturn().getResponse().getContentAsString();

		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(respContentAsStr, "$.content");
		assertThat(endpoints).hasSize(5);

		boolean prev = JsonPath.read(respContentAsStr, "$.prev");
		assertThat(prev).isTrue();

		int totalPages = JsonPath.read(respContentAsStr, "$.totalPages");
		assertThat(totalPages).isEqualTo(2);
	}

	private String setupMessageDeliveriesForSingleEvent() {
		GenericEvent event = eventRepo.save(GenericEventRepositoryTest.createProcessedEvent());

		String NAME = "john.doe";
		String EMAIL = "@objectify.sk";
		List<EmailEndpoint> emailEndpoints = IntStream.range(0, 5)
				.mapToObj(i -> EmailEndpoint.builder().email(NAME + i + EMAIL).build()).collect(toList());
		emailEndpoints.forEach(endpointRepo::persistEnpointIfNotExists);

		String PHONE = "090811111";
		List<SmsEndpoint> smsEndpoints = IntStream.range(0, 10)
				.mapToObj(i -> SmsEndpoint.builder().phone(PHONE + i).build()).collect(toList());
		smsEndpoints.forEach(endpointRepo::persistEnpointIfNotExists);

		EmailMessage emailMsg = new EmailMessage();
		emailMsg.setBody(EmailContent.builder().subject("Subject").text("Whatever").build());
		emailMsg.setReceivingEndpoints(emailEndpoints);
		emailMsg.setPreviousEventIds(Collections.singletonList(event.getEventId()));

		SmsMessage smsMsg = new SmsMessage();
		smsMsg.setBody(SimpleTextContent.builder().text("Whatever").build());
		smsMsg.setReceivingEndpoints(smsEndpoints);
		smsMsg.setPreviousEventIds(Collections.singletonList(event.getEventId()));

		messageProcessingFlow.processMessage(emailMsg);
		messageProcessingFlow.processMessage(smsMsg);

		Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> deliveryRepo.countByMessageIdAndStatus(emailMsg.getId(), SENT) >= 5);
		Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> deliveryRepo.countByMessageIdAndStatus(smsMsg.getId(), SENT) >= 10);

		return event.getExternalId();
	}

}

