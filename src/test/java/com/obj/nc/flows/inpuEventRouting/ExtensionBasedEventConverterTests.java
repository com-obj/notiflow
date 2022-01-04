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

package com.obj.nc.flows.inpuEventRouting;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.converterExtensions.genericEvent.InputEvent2IntentConverterExtension;
import com.obj.nc.converterExtensions.genericEvent.InputEvent2MessageConverterExtension;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersister;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

@ActiveProfiles(value = {"test"}, resolver = SystemPropertyActiveProfileResolver.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
public class ExtensionBasedEventConverterTests extends BaseIntegrationTest {

    @Autowired
    private GenericEventPersister persister;

    @Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
    @Autowired
    private SourcePollingChannelAdapter pollAbleSource;

    @BeforeEach
    public void startSourcePolling() {
        pollAbleSource.start();
    }

    @AfterEach
    public void stopSourcePolling() {
        pollAbleSource.stop();
    }


    @Test
    void testGenericEventRouting() {
        //GIVEN
        TestPayload pyload = new TestPayload(1, "value");

        GenericEvent event = GenericEvent.builder()
                .id(UUID.randomUUID())
                .payloadJson(JsonUtils.writeObjectToJSONNode(pyload))
                .build();

        //WHEN
        persister.accept(event);

        //THEN
        //one EmailMessage and one Intent resulting into second and third EmailMessage should be generated
        awaitSent(event.getId(), 3, Duration.ofSeconds(30));
    }

    @TestConfiguration
    public static class EventConvertionExtensionConfiguration {

        @Bean
        public InputEvent2MessageConverterExtension event2Message() {
            return new InputEvent2MessageConverterExtension() {

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
                    email1.getBody().setText("text");

                    return Collections.singletonList(email1);
                }
            };
        }

        @Bean
        public InputEvent2IntentConverterExtension event2Intent() {
            return new InputEvent2IntentConverterExtension() {

                @Override
                public Optional<PayloadValidationException> canHandle(GenericEvent payload) {
                    if (payload.getPayloadAsPojo(TestPayload.class) != null) {
                        return Optional.empty();
                    }

                    return Optional.of(new PayloadValidationException("No test payload"));
                }

                @Override
                public List<NotificationIntent> convert(GenericEvent event) {
                    NotificationIntent email1Intent = NotificationIntent.createWithStaticContent(
                            "Subject",
                            "Text",
                            EmailEndpoint.builder().email("test2@objectify.sk").build(),
                            EmailEndpoint.builder().email("test3@objectify.sk").build()
                    );

                    return Collections.singletonList(email1Intent);
                }
            };
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonTypeInfo(use = Id.CLASS)
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
