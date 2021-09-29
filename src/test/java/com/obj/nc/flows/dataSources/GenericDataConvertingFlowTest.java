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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
import org.assertj.core.api.AbstractObjectAssert;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
        // given
        TestPayload payload = TestPayload
                .builder()
                .num(3)
                .str("str")
                .build();
    
        GenericData genericData = GenericData
                .builder()
                .payloads(Arrays.asList(JsonUtils.writeObjectToJSONNode(payload)))
                .build();
    
        // when
        inputChannel.send(MessageBuilder.withPayload(genericData).build());
        
        // then
        Awaitility
                .await()
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> deliveryInfoRepository.countByStatus(SENT) >= 2);
    
        List<DeliveryInfo> infos = deliveryInfoRepository
                .findByStatus(SENT)
                .stream()
                .filter(info -> info.getMessageId() != null)
                .collect(Collectors.toList());

        assertThat(infos)
                .hasSize(2)
                .anySatisfy(assertRefersToMessageWithText("GenericData2NotificationConverterExtension"))
                .anySatisfy(assertRefersToMessageWithText("InputEvent2MessageConverterExtension"));
    }
    
    private Consumer<DeliveryInfo> assertRefersToMessageWithText(String text) {
        return any -> assertThat(any)
                .extracting(info -> messageRepository.findById(any.getMessageId()))
                .extracting(message -> message.get().getBody())
                .asInstanceOf(type(EmailContent.class))
                .extracting(content -> content.getText())
                .isEqualTo(text);
    }
    
    @TestConfiguration
    static class GenericDataConvertingFlowTestConfiguration {
        @Bean
        public GenericData2NotificationConverterExtension genericData2Message() {
            return new GenericData2NotificationConverterExtension() {
                @Override
                public Optional<PayloadValidationException> canHandle(GenericData payload) {
                    if (!payload.getPayloadsAsPojo(TestPayload.class).isEmpty()) {
                        return Optional.empty();
                    }
    
                    return Optional.of(new PayloadValidationException("No test payload"));
                }
    
                @Override
                public List<IsNotification> convert(GenericData payload) {
                    EmailMessage email1 = new EmailMessage();
                    email1.addReceivingEndpoints(
                            EmailEndpoint.builder().email("test@objectify.sk").build()
                    );
                    email1.getBody().setSubject("Subject");
                    email1.getBody().setText("GenericData2NotificationConverterExtension");
                    return Arrays.asList(email1);
                }
            };
        }
    
        @Bean
        public GenericData2EventConverterExtension genericData2Event() {
            return new GenericData2EventConverterExtension() {
                @Override
                public Optional<PayloadValidationException> canHandle(GenericData payload) {
                    if (!payload.getPayloadsAsPojo(TestPayload.class).isEmpty()) {
                        return Optional.empty();
                    }
                
                    return Optional.of(new PayloadValidationException("No test payload"));
                }
            
                @Override
                public List<GenericEvent> convert(GenericData payload) {
                    return Arrays.asList(
                            GenericEvent
                                    .builder()
                                    .id(UUID.randomUUID())
                                    .payloadJson(payload.getPayloads().get(0))
                                    .build()
                    );
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
                    email1.getBody().setText("InputEvent2MessageConverterExtension");
                
                    List<com.obj.nc.domain.message.Message<?>> msg = Arrays.asList(email1);
                
                    return msg;
                }
            };
        }
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public static class TestPayload {
        
        private Integer num;
        private String str;
    }
    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(
                    GreenMailConfiguration.aConfig()
                            .withUser("no-reply@objectify.sk", "xxx"))
            .withPerMethodLifecycle(true);
    
}