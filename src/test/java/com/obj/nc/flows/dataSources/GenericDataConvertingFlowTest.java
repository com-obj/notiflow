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

package com.obj.nc.flows.dataSources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.converterExtensions.genericData.GenericData2EventConverterExtension;
import com.obj.nc.converterExtensions.genericData.GenericData2NotificationConverterExtension;
import com.obj.nc.converterExtensions.genericEvent.InputEvent2MessageConverterExtension;
import com.obj.nc.domain.IsNotification;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.dataObject.GenericData;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.obj.nc.flows.dataSources.GenericDataConvertingFlowConfiguration.GENERIC_DATA_CONVERTING_FLOW_ID_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.SENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class GenericDataConvertingFlowTest extends BaseIntegrationTest {
    
    @Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
    @Autowired private SourcePollingChannelAdapter pollableSource;
    
    @Qualifier(GENERIC_DATA_CONVERTING_FLOW_ID_INPUT_CHANNEL_ID)
    @Autowired private MessageChannel inputChannel;
    
    @Autowired private DeliveryInfoRepository deliveryInfoRepository;
    @Autowired private MessageRepository messageRepository;
    
    @BeforeEach
    public void cleanTablesAndStartPollingEvents(@Autowired JdbcTemplate jdbcTemplate) {
        purgeNotifTables(jdbcTemplate);
        pollableSource.start();
    }
    
    @Test
    void testConvertGenericDataToMessageAndSend() {
        TestPayload payload = createTestPayload();
    
        GenericData<JsonNode> genericData = new GenericData<>(Collections.singletonList(JsonUtils.writeObjectToJSONNode(payload)));
    
        // when
        inputChannel.send(MessageBuilder.withPayload(genericData).build());
        
        // then
        assertMessageDelivered();
    }

    @Test
    void testConvertGenericDataToMessageAndSendUsingPojo() {
        // given
        TestPayload payload = createTestPayload();
    
        GenericData<TestPayload> genericData = new GenericData<>(Collections.singletonList(payload));
    
        // when
        inputChannel.send(MessageBuilder.withPayload(genericData).build());
        
        // then
        assertMessageDelivered();
    }

    private void assertMessageDelivered() {
        Awaitility
                .await()
                .atMost(5000, TimeUnit.SECONDS)
                .until(() -> findDeliveryInfosForMsg().size()>= 2);
    
        List<DeliveryInfo> infos = findDeliveryInfosForMsg();

        assertThat(infos)
                .hasSize(2)
                .anySatisfy(assertRefersToMessageWithText("GenericData2NotificationConverterExtension"))
                .anySatisfy(assertRefersToMessageWithText("InputEvent2MessageConverterExtension"));
    }

    private List<DeliveryInfo> findDeliveryInfosForMsg() {
        return deliveryInfoRepository
                .findByStatus(SENT)
                .stream()
                .filter(info -> info.getMessageId() != null)
                .collect(Collectors.toList());
    }

    private TestPayload createTestPayload() {
        return TestPayload
                .builder()
                .num(3)
                .str("str")
                .instant(Instant.now())
                .build();
    }    
    
    private Consumer<DeliveryInfo> assertRefersToMessageWithText(String text) {
        return any -> assertThat(any)
                .extracting(info -> messageRepository.findById(any.getMessageId()))
                .extracting(message -> message.get().getBody())
                .asInstanceOf(type(EmailContent.class))
                .extracting(EmailContent::getText)
                .asString()
                .contains(text);
    }
    
    @TestConfiguration
    static class GenericDataConvertingFlowTestConfiguration {
        @Bean
        public GenericData2NotificationConverterExtension<?> genericJsonData2Message() {
            return new GenericData2NotificationConverterExtension<JsonNode>() {
                @Override
                public Optional<PayloadValidationException> canHandle(GenericData<JsonNode> payload) {
                    if (!payload.getPayloadsAsPojo(TestPayload.class).isEmpty()) {
                        return Optional.empty();
                    }
    
                    return Optional.of(new PayloadValidationException("No test payload"));
                }
    
                @Override
                public List<IsNotification> convert(GenericData<JsonNode> payload) {
                    return convertTestPayloads(payload.getPayloadsAsPojo(TestPayload.class));
                }

                @Override
                public Class<JsonNode> getPayloadType() {
                    return JsonNode.class;
                }
            };
        }

        @Bean
        public GenericData2NotificationConverterExtension<?> genericPojoData2Message() {
            return new GenericData2NotificationConverterExtension<TestPayload>() {
                @Override
                public Optional<PayloadValidationException> canHandle(GenericData<TestPayload> data) {
                    if (!data.getPayloads().isEmpty()) {
                        return Optional.empty();
                    }
    
                    return Optional.of(new PayloadValidationException("No test payload"));
                }
    
                @Override
                public List<IsNotification> convert(GenericData<TestPayload> data) {
                    return convertTestPayloads(data.getPayloads());
                }

                @Override
                public Class<TestPayload> getPayloadType() {
                    return TestPayload.class;
                }
            };
        }

        public static List<IsNotification> convertTestPayloads(List<TestPayload> payload) {
            EmailMessage email1 = new EmailMessage();
            email1.addReceivingEndpoints(
                    EmailEndpoint.builder().email("test@objectify.sk").build()
            );
            email1.getBody().setSubject("Subject");
            email1.getBody().setText("GenericData2NotificationConverterExtension"+JsonUtils.writeObjectToJSONString(payload));
            return Collections.singletonList(email1);
        }
    
        @Bean
        public GenericData2EventConverterExtension<?> genericJsonData2Event() {
            return new GenericData2EventConverterExtension<JsonNode>() {
                @Override
                public Optional<PayloadValidationException> canHandle(GenericData<JsonNode> payload) {
                    if (!payload.getPayloadsAsPojo(TestPayload.class).isEmpty()) {
                        return Optional.empty();
                    }
                
                    return Optional.of(new PayloadValidationException("No test payload"));
                }
            
                @Override
                public List<GenericEvent> convert(GenericData<JsonNode> payload) {
                    return Collections.singletonList(
                            GenericEvent
                                    .builder()
                                    .id(UUID.randomUUID())
                                    .payloadJson(payload.getPayloads().get(0))
                                    .build()
                    );
                }

                @Override
                public Class<JsonNode> getPayloadType() {
                    return JsonNode.class;
                }
            };
        }

        @Bean
        public GenericData2EventConverterExtension<?> genericPojoData2Event() {
            return new GenericData2EventConverterExtension<TestPayload>() {
                @Override
                public Optional<PayloadValidationException> canHandle(GenericData<TestPayload> data) {
                    if (!data.getPayloads().isEmpty()) {
                        return Optional.empty();
                    }
                
                    return Optional.of(new PayloadValidationException("No test payload"));
                }
            
                @Override
                public List<GenericEvent> convert(GenericData<TestPayload> data) {
                    TestPayload testPayload = data.getPayloads().get(0);
                    return Collections.singletonList(
                            GenericEvent
                                    .builder()
                                    .id(UUID.randomUUID())
                                    .payloadJson(JsonUtils.readJsonNodeFromPojo(testPayload))
                                    .build()
                    );
                }

                @Override
                public Class<TestPayload> getPayloadType() {
                    return TestPayload.class;
                }
            };
        }
    
        @Bean
        public InputEvent2MessageConverterExtension event2Message() {
            return new InputEvent2MessageConverterExtension () {
            
                @Override
                public Optional<PayloadValidationException> canHandle(GenericEvent payload) {
                    if (payload.getPayloadAsPojo(TestPayload.class) != null) {
                        return Optional.empty();
                    }
                
                    return Optional.of(new PayloadValidationException("No test payload"));
                }
            
                @Override
                public List<Message<?>> convert(GenericEvent event) {
                    EmailMessage email1 = new EmailMessage();
                    email1.addReceivingEndpoints(
                            EmailEndpoint.builder().email("test@objectify.sk").build()
                    );
                    email1.getBody().setSubject("Subject");
                    email1.getBody().setText("InputEvent2MessageConverterExtension"+JsonUtils.writeObjectToJSONString(event.getPayloadAsPojo(TestPayload.class)));
                
                    return Collections.singletonList(email1);
                }
            };
        }
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TestPayload {
        
        private Integer num;
        private String str;
        private Instant instant;
        
    }
    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(
                    GreenMailConfiguration.aConfig()
                            .withUser("no-reply@objectify.sk", "xxx"))
            .withPerMethodLifecycle(true);
    
}