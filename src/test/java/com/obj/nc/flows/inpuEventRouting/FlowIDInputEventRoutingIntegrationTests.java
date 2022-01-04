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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersister;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.MessagingException;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
public class FlowIDInputEventRoutingIntegrationTests extends BaseIntegrationTest {
	
	@Autowired private GenericEventPersister persister;
	@Qualifier("TEST_FLOW_INPUT")
	@Autowired private PollableChannel flowInputChannel;
	
	@Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
	@Autowired private SourcePollingChannelAdapter pollAbleSource;
	
    @BeforeEach
    public void startSourcePolling() {
    	pollAbleSource.start();    	
    }
	
    @Test
    void testGenericEventRouting() throws MessagingException {	
        GenericEvent event = GenericEvent.from(JsonUtils.readJsonNodeFromClassPathResource("events/generic_event.json"));
        event.setFlowId("TEST_FLOW"); //this maps to input channel name
        
        persister.accept(event);
             
        Message<?> springMessage = flowInputChannel.receive(5000);
	    assertEventReadyForProcessing(springMessage);
    }
    
    @AfterEach
    public void stopSourcePolling() {
    	pollAbleSource.stop();
    }

	public static void assertEventReadyForProcessing(Message<?> springMessage) {
		assertThat(springMessage).isNotNull();
	    assertThat(springMessage.getPayload()).isInstanceOf(GenericEvent.class);	    
	}

    @TestConfiguration
    public static class TestModeTestConfiguration {

        @Bean("TEST_FLOW_INPUT")
        public PollableChannel flowInputChannel() {
            return new QueueChannel();
        }

    }
    
    @Data
    @NoArgsConstructor
    @JsonTypeInfo(include = As.PROPERTY, use = Id.NAME)
    @JsonSubTypes({ 
    	@Type(value = TestPayload.class, name = "TYPE_3")})
    public static class TestPayload {
    	
    	private Integer num;
    	private String str;
    }

}
