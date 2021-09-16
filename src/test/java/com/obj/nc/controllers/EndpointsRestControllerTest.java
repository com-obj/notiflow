/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.controllers;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.PROCESSING;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.SENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.JsonUtils;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.jayway.jsonpath.JsonPath;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.flows.messageProcessing.MessageProcessingFlow;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class EndpointsRestControllerTest extends BaseIntegrationTest {
	
	@Autowired private EndpointsRepository endpointsRepository;
	@Autowired private DeliveryInfoRepository deliveryInfoRepository;
	@Autowired private MessageRepository messageRepository;
	@Autowired private GenericEventRepository genericEventRepository;
	@Autowired private MockMvc mockMvc;
	@Autowired private MessageProcessingFlow messageProcessingFlow;
	
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
    void testFindAllEndpoints() throws Exception {
    	//GIVEN
		persistTestEndpoints();
	
		//WHEN TEST REST
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.get("/endpoints")
                .contentType(APPLICATION_JSON_UTF8)
        		.accept(APPLICATION_JSON_UTF8))
        		.andDo(MockMvcResultHandlers.print());
        
        //THEN
		resp
        	.andExpect(status().is2xxSuccessful());
	
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(2);
		assertContainsEndpoint(endpoints, "john.doe@objectify.sk", 0);
		assertContainsEndpoint(endpoints, "+999999999999", 0);
	}
	
	@Test
	void testCountSentMessages() throws Exception {
		//GIVEN
		persistTestEndpointsAndTestDeliveryInfos();
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is2xxSuccessful());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(3);
		assertContainsEndpoint(endpoints, "john.doe@gmail.com", 1);
		assertContainsEndpoint(endpoints, "john.dudly@gmail.com", 1);
		assertContainsEndpoint(endpoints, "0908186997", 0);
	}
	
	@Test
	void testFilterDateRangeEndpointsStartAndEnd() throws Exception {
		//GIVEN
		persistTestEndpointsAndTestDeliveryInfos();
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("startAt", Instant.now().minusSeconds(60).toString())
						.param("endAt", Instant.now().plusSeconds(60).toString())
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is2xxSuccessful());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(3);
		assertContainsEndpoint(endpoints, "john.doe@gmail.com", 1);
		assertContainsEndpoint(endpoints, "john.dudly@gmail.com", 1);
		assertContainsEndpoint(endpoints, "0908186997", 0);
	}
	
	@Test
	void testFilterDateRangeEndpointsStartOnly() throws Exception {
		//GIVEN
		persistTestEndpointsAndTestDeliveryInfos();
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("startAt", Instant.now().minusSeconds(60).toString())
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is2xxSuccessful());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(3);
		assertContainsEndpoint(endpoints, "john.doe@gmail.com", 1);
		assertContainsEndpoint(endpoints, "john.dudly@gmail.com", 1);
		assertContainsEndpoint(endpoints, "0908186997", 0);
	}
	
	@Test
	void testFilterDateRangeEndpointsEndOnly() throws Exception {
		//GIVEN
		persistTestEndpointsAndTestDeliveryInfos();
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("endAt", Instant.now().plusSeconds(60).toString())
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is2xxSuccessful());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(3);
		assertContainsEndpoint(endpoints, "john.doe@gmail.com", 1);
		assertContainsEndpoint(endpoints, "john.dudly@gmail.com", 1);
		assertContainsEndpoint(endpoints, "0908186997", 0);
	}
	
	@Test
	void testFilterEndpointTypeSMS() throws Exception {
		//GIVEN
		persistTestEndpoints();
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("endpointType", "SMS")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is2xxSuccessful());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(1);
		assertContainsEndpoint(endpoints, "+999999999999", 0);
	}
	
	@Test
	void testFilterInvalidEndpointType() throws Exception {
		//GIVEN
		persistTestEndpoints();
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("endpointType", "INVALID123")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is5xxServerError());
	}
	
	@Test
	void testFilterEndpointsByEventId() throws Exception {
		//GIVEN
		UUID eventId = persistTestEndpointsForEventIdFiltering();
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("eventId", eventId.toString())
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.content.size()", Matchers.is(2)));
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertContainsEndpoint(endpoints, "john.doe@gmail.com", 0);
		assertContainsEndpoint(endpoints, "john.dudly@gmail.com", 0);
	}
	
	@Test
	void testGetPage0Size20() throws Exception {
		// GIVEN
		persistNEmailEndpoints(19);
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("page", "0")
						.param("size", "20")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(19);
	}
	
	@Test
	void testGetPage0Size10() throws Exception {
    	// GIVEN
		persistNEmailEndpoints(19);
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("page", "0")
						.param("size", "10")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(10);
	}
	
	@Test
	void testGetPage1Size10() throws Exception {
		// GIVEN
		persistNEmailEndpoints(19);
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("page", "1")
						.param("size", "10")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(9);
	}
	
	@Test
	void testFindEndpointById() throws Exception {
		// given
		EmailEndpoint email = EmailEndpoint.builder().email("john.doe@objectify.sk").build();
		email = endpointsRepository.persistEnpointIfNotExists(email);
		
		// when
		mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints/{endpointId}", email.getId())
						.accept(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.id", Matchers.is(email.getId().toString())))
				.andExpect(jsonPath("$.endpointId", Matchers.is("john.doe@objectify.sk")))
				.andDo(MockMvcResultHandlers.print());
	}
	
	private void persistTestEndpoints() {
		EmailEndpoint email = EmailEndpoint.builder().email("john.doe@objectify.sk").build();
		endpointsRepository.persistEnpointIfNotExists(email);
		
		SmsEndpoint phone = SmsEndpoint.builder().phone("+999999999999").build();
		endpointsRepository.persistEnpointIfNotExists(phone);
	}
	
	private UUID persistTestEndpointsForEventIdFiltering() {
		GenericEvent event = GenericEvent
				.builder()
				.id(UUID.randomUUID())
				.payloadJson(JsonUtils.readJsonNodeFromJSONString(""))
				.build();
		event = genericEventRepository.save(event);
		
		EmailEndpoint email1 = EmailEndpoint.builder().email("john.doe@gmail.com").build();
		endpointsRepository.persistEnpointIfNotExists(email1);
		EmailEndpoint email2 = EmailEndpoint.builder().email("john.dudly@gmail.com").build();
		endpointsRepository.persistEnpointIfNotExists(email2);
		EmailEndpoint email3 = EmailEndpoint.builder().email("john.wick@gmail.com").build();
		endpointsRepository.persistEnpointIfNotExists(email3);
		
		EmailMessage emailMessage = new EmailMessage();
		emailMessage.addPreviousEventId(event.getId());
		emailMessage.setBody(EmailContent.builder().subject("Subject").text("Text").build());
		emailMessage.setReceivingEndpoints(Arrays.asList(email1, email2));
		messageRepository.save(emailMessage.toPersistentState());
		
		return event.getId();
	}
	
	private void persistTestEndpointsAndTestDeliveryInfos() {
		EmailEndpoint email1 = EmailEndpoint.builder().email("john.doe@gmail.com").build();
		EmailEndpoint email2 = EmailEndpoint.builder().email("john.dudly@gmail.com").build();
		
		EmailMessage emailMessage = new EmailMessage();
		emailMessage.setBody(EmailContent.builder().subject("Subject").text("Text").build());
		emailMessage.setReceivingEndpoints(Arrays.asList(email1, email2));
		messageProcessingFlow.processMessage(emailMessage);
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> deliveryInfoRepository.countByMessageIdAndStatus(emailMessage.getId(), SENT) >= 2);
		
		SmsEndpoint sms = SmsEndpoint.builder().phone("0908186997").build();
		
		SmsMessage smsMessage = new SmsMessage();
		smsMessage.setBody(SimpleTextContent.builder().text("Text").build());
		smsMessage.setReceivingEndpoints(Arrays.asList(sms));
		messageProcessingFlow.processMessage(smsMessage);
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> deliveryInfoRepository.countByMessageIdAndStatus(smsMessage.getId(), PROCESSING) >= 1);
	}
	
	private void assertContainsEndpoint(List<LinkedHashMap<?, ?>> endpoints, String endpointName, long sentMessagesCount) {
		Optional<LinkedHashMap<?, ?>> emailDto = endpoints.stream().filter(endpoint -> endpointName.equals(endpoint.get("name"))).findFirst();
		assertThat(emailDto).isNotNull();
		assertThat(Long.valueOf(emailDto.get().get("sentMessagesCount").toString())).isEqualTo(sentMessagesCount);
	}
	
	private void awaitMessageAndDeliveryInfos(int numberToWait) {
		MessagePersistentState sentMessage = waitForFirstMessage();
		
		Awaitility.await().atMost(Duration.ofSeconds(3)).until(
				() -> deliveryInfoRepository.findByMessageIdOrderByProcessedOn(sentMessage.getId()).size() >= numberToWait
		);
	}

	public MessagePersistentState waitForFirstMessage() {
		Awaitility.await().atMost(Duration.ofSeconds(3)).until(
				() -> messageRepository.findAll().iterator().next() != null
		);
		
		MessagePersistentState sentMessage = messageRepository.findAll().iterator().next();		
		return sentMessage;
	}
	
	private void persistNEmailEndpoints(int n) {
		for (int i = 0; i < n; i++) {
			String name = "john.doe";
			EmailEndpoint email = EmailEndpoint.builder().email(name + i + "@objectify.sk").build();
			endpointsRepository.persistEnpointIfNotExists(email);
		}
	}
	
}
