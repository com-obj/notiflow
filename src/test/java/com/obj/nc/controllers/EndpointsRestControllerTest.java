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
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.flows.messageProcessing.MessageProcessingFlow;
import com.obj.nc.functions.processors.delivery.MessageAndEndpointPersister;
import com.obj.nc.functions.processors.senders.SmsSender;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.PROCESSING;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.SENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class EndpointsRestControllerTest extends BaseIntegrationTest {

	@Autowired private EndpointsRepository endpointsRepository;
	@Autowired private DeliveryInfoRepository deliveryInfoRepository;
	@Autowired private GenericEventRepository genericEventRepository;
	@Autowired private MockMvc mockMvc;
	@Autowired private MessageProcessingFlow messageProcessingFlow;
	@Autowired private MessageAndEndpointPersister messageAndEndpointPersister;
	@MockBean
	SmsSender smsSender;

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
						.param("processedFrom", Instant.now().minusSeconds(60).toString())
						.param("processedTo", Instant.now().plusSeconds(60).toString())
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
						.param("processedFrom", Instant.now().minusSeconds(60).toString())
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
						.param("processedTo", Instant.now().plusSeconds(60).toString())
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
	void testFilterEndpointsByEndpointId() throws Exception {
		//GIVEN
		UUID endpointId = persistTestEndpointsForEndpointIdFiltering();

		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("endpointId", endpointId.toString())
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());

		//THEN
		resp
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.content.size()", Matchers.is(1)));

		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertContainsEndpoint(endpoints, "john.doe@gmail.com", 0);
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

		EmailMessage emailMessage = new EmailMessage();
		emailMessage.addPreviousEventId(event.getId());
		emailMessage.setBody(EmailContent.builder().subject("Subject").text("Text").build());
		emailMessage.setReceivingEndpoints(Arrays.asList(
				EmailEndpoint.builder().email("john.doe@gmail.com").build(),
				EmailEndpoint.builder().email("john.dudly@gmail.com").build()));

		messageAndEndpointPersister.apply(emailMessage);

		return event.getId();
	}

	private UUID persistTestEndpointsForEndpointIdFiltering() {
		EmailEndpoint email1 = EmailEndpoint.builder().email("john.doe@gmail.com").build();
		endpointsRepository.persistEnpointIfNotExists(email1);
		EmailEndpoint email2 = EmailEndpoint.builder().email("john.dudly@gmail.com").build();
		endpointsRepository.persistEnpointIfNotExists(email2);
		EmailEndpoint email3 = EmailEndpoint.builder().email("john.wick@gmail.com").build();
		endpointsRepository.persistEnpointIfNotExists(email3);
		return email1.getId();
	}

	private void persistTestEndpointsAndTestDeliveryInfos() {
		EmailEndpoint email1 = EmailEndpoint.builder().email("john.doe@gmail.com").build();
		EmailEndpoint email2 = EmailEndpoint.builder().email("john.dudly@gmail.com").build();

		EmailMessage emailMessage = new EmailMessage();
		emailMessage.setBody(EmailContent.builder().subject("Subject").text("Text").build());
		emailMessage.setReceivingEndpoints(Arrays.asList(email1, email2));
		messageProcessingFlow.processMessage(emailMessage);
		await().atMost(15, TimeUnit.SECONDS).until(() -> deliveryInfoRepository.countByMessageIdAndStatus(emailMessage.getId(), SENT) >= 2);

		SmsEndpoint sms = SmsEndpoint.builder().phone("0908186997").build();

		SmsMessage smsMessage = new SmsMessage();
		smsMessage.setBody(SimpleTextContent.builder().text("Text").build());
		smsMessage.setReceivingEndpoints(Arrays.asList(sms));

		Mockito.when(smsSender.apply(smsMessage)).thenReturn(smsMessage);

		messageProcessingFlow.processMessage(smsMessage);
		await().atMost(5, TimeUnit.SECONDS).until(() -> deliveryInfoRepository.countByMessageIdAndStatus(smsMessage.getId(), PROCESSING) >= 1);
	}

	private void assertContainsEndpoint(List<LinkedHashMap<?, ?>> endpoints, String endpointName, long sentMessagesCount) {
		Optional<LinkedHashMap<?, ?>> emailDto = endpoints.stream().filter(endpoint -> endpointName.equals(endpoint.get("name"))).findFirst();
		assertThat(emailDto).isNotNull();
		assertThat(Long.valueOf(emailDto.get().get("messagesSentCount").toString())).isEqualTo(sentMessagesCount);
	}

	private void persistNEmailEndpoints(int n) {
		for (int i = 0; i < n; i++) {
			String name = "john.doe";
			EmailEndpoint email = EmailEndpoint.builder().email(name + i + "@objectify.sk").build();
			endpointsRepository.persistEnpointIfNotExists(email);
		}
	}

}
