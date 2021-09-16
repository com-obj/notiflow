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

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

import javax.mail.MessagingException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.obj.nc.domain.IsTypedJson;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersister;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest(properties = {
		"nc.flows.input-evet-routing.type-propery-name=@type",
		"nc.flows.input-evet-routing.type-channel-mapping.TYPE_1=CHANNEL_1",
		"nc.flows.input-evet-routing.type-channel-mapping.TYPE_2=CHANNEL_2"})
public class TypeIDInputEventRoutingIntegrationTests extends BaseIntegrationTest {
	
	@Autowired private GenericEventPersister persister;
	
	@Qualifier("CHANNEL_1")
	@Autowired private PollableChannel flowInputChannel1;
	@Qualifier("CHANNEL_2")
	@Autowired private PollableChannel flowInputChannel2;
	
	@Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
	@Autowired private SourcePollingChannelAdapter pollableSource;
	
	@Value("${nc.flows.input-evet-routing.type-channel-mapping.TYPE_1}")
	private String value;
	
    @BeforeEach
    public void startSourcePolling() {
    	pollableSource.start();
    	
    	JsonUtils.resetObjectMapper();
    	JsonUtils.getObjectMapper().addMixIn(IsTypedJson.class, TestPayload.class);
    }

    
    @Test
    void testGenericEventRouting() throws MessagingException {
    	//WHEN
        GenericEvent event = GenericEvent.from(JsonUtils.readJsonNodeFromClassPathResource("events/generic_event_with_type_info1.json"));    
        persister.accept(event);
        
        //THEN
	    Message<?> springMessage = flowInputChannel1.receive(5000);
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
    
    
    @Data
    @NoArgsConstructor
    @JsonTypeInfo(include = As.PROPERTY, use = Id.NAME)
    @JsonSubTypes({ 
    	@Type(value = TestPayload.class, name = "TYPE_1"),
    	@Type(value = TestPayload.class, name = "TYPE_2")})
    public static class TestPayload implements IsTypedJson {
    	
    	private Integer num;
    	private String str;
    }

}
