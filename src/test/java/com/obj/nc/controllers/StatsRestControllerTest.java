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
import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.domain.recipients.Recipient;
import com.obj.nc.extensions.providers.recipients.ContactsProvider;
import com.obj.nc.flows.intenProcessing.NotificationIntentProcessingFlow;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import lombok.Builder;
import lombok.Data;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.*;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest(properties = {
        "nc.contacts-store.jsonStorePathAndFileName=src/test/resources/contact-store/contact-store.json", 
})
class StatsRestControllerTest extends BaseIntegrationTest {
    
    @Autowired private GenericEventRepository genericEventRepository;
    @Autowired private EndpointsRepository endpointsRepository;
    @Autowired private DeliveryInfoRepository deliveryInfoRepository;
    @Autowired private NotificationIntentProcessingFlow intentProcessingFlow;
    @Autowired private NcAppConfigProperties ncAppConfigProperties;
    @Autowired protected MockMvc mockMvc;
    @Autowired private ContactsProvider contactStore;
    
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

    @AfterEach
    public void waitForIntegrationFlowsToFinish() {
        super.wiatForIntegrationFlowsToFinish(500000);
    }
    
    @Test

    void testFindEventStatsByEventId() throws Exception {
        // GIVEN
        GenericEvent event = processTestEventAndIntent();
    
        //WHEN
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.get("/stats/events/{eventId}", event.getId())
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8));
        // THEN
        resp
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.eventsCount").value(CoreMatchers.is(1)))
                .andExpect(jsonPath("$.intentsCount").value(CoreMatchers.is(1)))
                .andExpect(jsonPath("$.messagesCount").value(CoreMatchers.is(4)))
                .andExpect(jsonPath("$.endpointsCount").value(CoreMatchers.is(2)))
                .andExpect(jsonPath("$.messagesSentCount").value(CoreMatchers.is(1)))
                .andExpect(jsonPath("$.messagesReadCount").value(CoreMatchers.is(1)))
                .andExpect(jsonPath("$.messagesFailedCount").value(CoreMatchers.is(2)));

        //AND WHEN
        List<ReceivingEndpoint> endpoints = endpointsRepository.findByNameIds("john.doe@objectify.sk");

        resp = mockMvc
                .perform(MockMvcRequestBuilders.get("/stats/endpoints/{endpointId}", endpoints.get(0).getId())
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        // THEN
        resp
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.eventsCount").value(CoreMatchers.is(1)))
                .andExpect(jsonPath("$.messagesCount").value(CoreMatchers.is(2)))
                .andExpect(jsonPath("$.endpointsCount").value(CoreMatchers.is(1)))
                .andExpect(jsonPath("$.messagesSentCount").value(CoreMatchers.is(1)))
                .andExpect(jsonPath("$.messagesReadCount").value(CoreMatchers.is(1)))
                .andExpect(jsonPath("$.messagesFailedCount").value(CoreMatchers.is(0)));
    }
    
    private GenericEvent processTestEventAndIntent() throws Exception {
        GenericEvent event = GenericEvent.builder()
                .id(UUID.randomUUID())
                .flowId("default-flow")
                .payloadJson(JsonUtils.readJsonNodeFromPojo(TestPayload.builder().value("Test").build()))
                .timeConsumed(Instant.now())
                .build();
        event = genericEventRepository.save(event);
        
        ReceivingEndpoint emailEndpoint = contactStore.findEndpoint("john.doe@objectify.sk");
        ReceivingEndpoint emailEndpoint2 = contactStore.findEndpoint("invalid mail");
        
        NotificationIntent intent = NotificationIntent.createWithStaticContent("Subject", "Text");
        intent.addRecipientsByName("John Doe", "Invalid");
        intent.getHeader().setFlowId("default-flow");
        intent.addPreviousEventId(event.getId());
        
        intentProcessingFlow.processNotificationIntent(intent);
        
        await().atMost(5, TimeUnit.SECONDS).until(() ->  findSentDeliveryInfosForMessage(emailEndpoint.getId(),SENT).size() >= 1);
        List<DeliveryInfo> sentInfos = findSentDeliveryInfosForMessage(emailEndpoint.getId(), SENT);
        
        ResultActions resp1 = mockMvc
                .perform(MockMvcRequestBuilders
                        .get(ncAppConfigProperties.getContextPath() + "/delivery-info/messages/{messageId}/mark-as-read", Objects.requireNonNull(sentInfos.get(0).getMessageId()).toString())
                        .contextPath(ncAppConfigProperties.getContextPath())
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        await().atMost(5, TimeUnit.SECONDS).until(() -> deliveryInfoRepository.countByMessageIdAndStatus(sentInfos.get(0).getMessageId(), READ) >= 1);
        await().atMost(5, TimeUnit.SECONDS).until(() ->  findSentDeliveryInfosForMessage(emailEndpoint2.getId(),FAILED).size() >= 1);
        return event;
    }

    private List<DeliveryInfo> findSentDeliveryInfosForMessage(UUID endpointId, DeliveryInfo.DELIVERY_STATUS status) {
        return deliveryInfoRepository
            .findByEndpointIdOrderByProcessedOn(endpointId)
            .stream()
            .filter(sentInfo -> sentInfo.getMessageId() != null)
            .filter(sentInfo -> sentInfo.getStatus() == status)
            .collect(Collectors.toList());
    }
    
    @Data
    @Builder
    private static class TestPayload {
        String value;
    }
    
}
