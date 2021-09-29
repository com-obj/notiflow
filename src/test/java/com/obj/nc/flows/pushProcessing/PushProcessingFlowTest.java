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

package com.obj.nc.flows.pushProcessing;

import com.obj.nc.domain.content.push.PushContent;
import com.obj.nc.domain.endpoints.push.DirectPushEndpoint;
import com.obj.nc.domain.endpoints.push.PushEndpoint;
import com.obj.nc.domain.endpoints.push.TopicPushEndpoint;
import com.obj.nc.domain.message.PushMessage;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.senders.PushSender;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class PushProcessingFlowTest extends BaseIntegrationTest {
    
    @Autowired private PushProcessingFlow pushProcessingFlow;
    @Autowired private EndpointsRepository endpointsRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private DeliveryInfoRepository deliveryInfoRepository;
    
    @MockBean private PushSender pushSender;
    
    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
        purgeNotifTables(jdbcTemplate);

    }

    @Test
    void testSendTokenPush() {
        // given
        PushMessage message = new PushMessage();
        message.setBody(
                PushContent.builder()
                        .subject("Subject")
                        .text("TEST")
                        .build()
        );
        message.setReceivingEndpoints(
                Arrays.asList(PushEndpoint.ofToken("test-token"))
        );
        
        Mockito
                .when(pushSender.apply(message))
                .thenReturn(message);
        
        // when
        pushProcessingFlow.sendPushMessage(message);
    
        Awaitility
                .await()
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> deliveryInfoRepository
                        .countByMessageIdAndStatus(message.getId(), DeliveryInfo.DELIVERY_STATUS.SENT) > 0);
        
        // then
        assertFlowSucceeded("test-token", DirectPushEndpoint.JSON_TYPE_IDENTIFIER, message.getId());
    }
    
    @Test
    void testSendTopicPush() {
        // given
        PushMessage message = new PushMessage();
        message.setBody(
                PushContent.builder()
                        .subject("Subject")
                        .text("TEST")
                        .build()
        );
        message.setReceivingEndpoints(
                Arrays.asList(PushEndpoint.ofTopic("test-topic"))
        );
        
        Mockito
                .when(pushSender.apply(message))
                .thenReturn(message);
        
        // when
        pushProcessingFlow.sendPushMessage(message);
        
        Awaitility
                .await()
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> deliveryInfoRepository
                        .countByMessageIdAndStatus(message.getId(), DeliveryInfo.DELIVERY_STATUS.SENT) > 0);
        
        // then
        assertFlowSucceeded("test-topic", TopicPushEndpoint.JSON_TYPE_IDENTIFIER, message.getId());
    }
    
    private void assertFlowSucceeded(String endpointId, String endpointType, UUID messageId) {
        assertThat(endpointsRepository.findAllEndpoints().get(0))
                .satisfies(endpoint -> {
                    assertThat(endpoint.getEndpointId()).isEqualTo(endpointId);
                    assertThat(endpoint.getEndpointType()).isEqualTo(endpointType);
                });
        
        assertThat(messageRepository.findAll().iterator().next().getBody())
                .asInstanceOf(type(PushContent.class))
                .satisfies(content -> {
                    assertThat(content.getSubject()).isEqualTo("Subject");
                    assertThat(content.getText()).isEqualTo("TEST");
                    assertThat(content.getIconUrl()).isNull();
                });
        
        assertThat(deliveryInfoRepository.countByMessageIdAndStatus(messageId, DeliveryInfo.DELIVERY_STATUS.SENT))
                .isEqualTo(1);
    }
    
}