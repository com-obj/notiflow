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

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersister;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.context.ActiveProfiles;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

@ActiveProfiles(value = {"test"}, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
        (properties = {
                "nc.flows.input-evet-routing.type-propery-name=@type",
                "nc.flows.input-evet-routing.type-channel-mapping.TYPE_1=CHANNEL_1",
                "nc.flows.input-evet-routing.type-channel-mapping.TYPE_2=CHANNEL_2"})
public class TypeIDInputEventRoutingIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private GenericEventPersister persister;

    @Qualifier("CHANNEL_1")
    @Autowired
    private PollableChannel flowInputChannel1;

    @Qualifier("CHANNEL_2")
    @Autowired
    private PollableChannel flowInputChannel2;

    @Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
    @Autowired
    private SourcePollingChannelAdapter pollableSource;

    @BeforeEach
    public void startSourcePolling() {
        pollableSource.start();
    }


    @Test
    void testGenericEventRouting() {
        //WHEN
        GenericEvent event = GenericEvent.from(JsonUtils.readJsonNodeFromClassPathResource("events/generic_event_with_type_info1.json"));
        persister.accept(event);

        //THEN
        Message<?> springMessage = flowInputChannel1.receive(15000);
        FlowIDInputEventRoutingIntegrationTests.assertEventReadyForProcessing(springMessage);

        //WHEN
        event = GenericEvent.from(JsonUtils.readJsonNodeFromClassPathResource("events/generic_event_with_type_info2.json"));
        persister.accept(event);

        //THEN
        springMessage = flowInputChannel2.receive(5000);
        FlowIDInputEventRoutingIntegrationTests.assertEventReadyForProcessing(springMessage);
    }

    @AfterEach
    public void stopSourcePolling() {
        pollableSource.stop();
    }

    @TestConfiguration
    public static class TestModeTestConfiguration {

        @Bean("CHANNEL_1")
        public PollableChannel flowInputChannel1() {
            return new QueueChannel();
        }

        @Bean("CHANNEL_2")
        public PollableChannel flowInputChannel2() {
            return new QueueChannel();
        }

    }

    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(
                    GreenMailConfiguration.aConfig()
                            .withUser("no-reply@objectify.sk", "xxx"))
            .withPerMethodLifecycle(true);
}
