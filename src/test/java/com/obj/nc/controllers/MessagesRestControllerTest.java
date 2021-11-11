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
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.domain.message.SendEmailMessageRequest;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.DateFormatMatcher;
import com.obj.nc.utils.JsonUtils;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class MessagesRestControllerTest extends BaseIntegrationTest {
    
    @Autowired private MockMvc mockMvc;
    @Autowired private MessageRepository messageRepository;
    @Autowired private EndpointsRepository endpointsRepository;
    @Autowired private GenericEventRepository genericEventRepository;
    @Autowired private DeliveryInfoRepository deliveryInfoRepository;
    
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
    void testSendEmailMessage() throws Exception {
        // given
        SendEmailMessageRequest sendEmailRequest = SendEmailMessageRequest
                .builder()
                .subject("subject")
                .text("text")
                .to(Arrays.asList(
                        SendEmailMessageRequest.Recipient
                                .builder()
                                .email("johndoe@objectify.sk")
                                .name("john doe")
                                .build(),
                        SendEmailMessageRequest.Recipient
                                .builder()
                                .email("johndudly@objectify.sk")
                                .name("john dudly")
                                .build()))
                .build();
        
        //when
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/messages/send-email")
        		.contentType(APPLICATION_JSON_UTF8)
        		.content(JsonUtils.writeObjectToJSONString(sendEmailRequest))
                .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.ncMessageId").value(CoreMatchers.notNullValue()));
        
        String messageId = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.ncMessageId");
        UUID messageUUID = UUID.fromString(messageId);
        
        Awaitility
                .await()
                .atMost(15, TimeUnit.SECONDS)
                .until(() -> deliveryInfoRepository.countByMessageIdAndStatus(messageUUID, DeliveryInfo.DELIVERY_STATUS.SENT) >= 2);
        
        assertThat(deliveryInfoRepository.countByMessageIdAndStatus(messageUUID, DeliveryInfo.DELIVERY_STATUS.SENT))
                .isEqualTo(2);
    }
    
    @Test
    void testFindAllMessages() throws Exception {
        // given
        persistTestEmailMessages(19);
        
        // when - then
        mockMvc
                .perform(MockMvcRequestBuilders
                        .get("/messages")
                        .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.content.size()", Matchers.is(19)))
                .andExpect(jsonPath("$.content[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.content[0].timeCreated", DateFormatMatcher.matchesISO8601()))
                .andExpect(jsonPath("$.content[0].endpointIds", Matchers.not(Matchers.emptyArray())));
    }
    
    @Test
    void testFindAllMessagesTimeCreatedFromFilter() throws Exception {
        // given
        persistTestEmailMessages(19);
    
        // when - then
        mockMvc
                .perform(MockMvcRequestBuilders
                        .get("/messages")
                        .param("createdFrom", Instant.now().minus(10, ChronoUnit.DAYS).toString())
                        .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.content.size()", Matchers.is(10)));
    }
    
    @Test
    void testFindAllMessagesTimeCreatedToFilter() throws Exception {
        // given
        persistTestEmailMessages(19);
        
        // when - then
        mockMvc
                .perform(MockMvcRequestBuilders
                        .get("/messages")
                        .param("createdTo", Instant.now().minus(10, ChronoUnit.DAYS).toString())
                        .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.content.size()", Matchers.is(9)));
    }
    
    @Test
    void testFindAllMessagesTimeCreatedFromAndToFilter() throws Exception {
        // given
        persistTestEmailMessages(19);
        
        // when - then
        mockMvc
                .perform(MockMvcRequestBuilders
                        .get("/messages")
                        .param("createdFrom", Instant.now().minus(15, ChronoUnit.DAYS).toString())
                        .param("createdTo", Instant.now().minus(10, ChronoUnit.DAYS).toString())
                        .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.content.size()", Matchers.is(5)));
    }
    
    @Test
    void testFindAllMessagesEventIdFilter() throws Exception {
        // given
        UUID eventId = persistTestEmailMessages(9);
    
        // when - then
        mockMvc
                .perform(MockMvcRequestBuilders
                        .get("/messages")
                        .param("eventId", eventId.toString())
                        .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.content.size()", Matchers.is(9)));
    }
    
    @Test
    void testFindAllMessagesPage0Size10() throws Exception {
        // given
        persistTestEmailMessages(19);
        
        // when - then
        mockMvc
                .perform(MockMvcRequestBuilders
                        .get("/messages")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.content.size()", Matchers.is(10)));
    }
    
    @Test
    void testFindAllMessagesPage1Size10() throws Exception {
        // given
        persistTestEmailMessages(19);
        
        // when - then
        mockMvc
                .perform(MockMvcRequestBuilders
                        .get("/messages")
                        .param("page", "1")
                        .param("size", "10")
                        .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.content.size()", Matchers.is(9)));
    }
    
    private UUID persistTestEmailMessages(long n) {
        UUID eventId = UUID.randomUUID();
    
        GenericEvent event = GenericEvent.builder()
                .id(eventId)
                .flowId("default-flow")
                .payloadJson(JsonUtils.readJsonNodeFromJSONString("{\"test\": \"test\"}"))
                .timeConsumed(Instant.now())
                .build();
        genericEventRepository.save(event);
        
        for (long i = 0; i < n; i++) {
            EmailMessage message = new EmailMessage();
            message.addPreviousEventId(eventId);
            
            message.setBody(
                    EmailContent
                            .builder()
                            .subject("subject")
                            .text("text")
                            .build());
    
            EmailEndpoint endpoint = EmailEndpoint
                    .builder()
                    .email(String.format("johndoe%d@objecify.sk", i))
                    .build();
            
            message.addReceivingEndpoints(endpoint);
            endpointsRepository.persistEnpointIfNotExists(endpoint);
            
            MessagePersistentState persistedMessage = messageRepository.save(message.toPersistentState());
            persistedMessage.setTimeCreated(Instant.now().minus(i, ChronoUnit.DAYS));
            messageRepository.save(persistedMessage);
        }
        
        Awaitility
                .await()
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> messageRepository.count() >= n);
        
        return eventId;
    }
    
}
