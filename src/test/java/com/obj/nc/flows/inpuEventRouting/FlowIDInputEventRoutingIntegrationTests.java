package com.obj.nc.flows.inpuEventRouting;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import javax.mail.MessagingException;

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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.obj.nc.domain.IsTypedJson;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersisterConsumer;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest(properties = {
		"nc.flows.input-evet-routing.type=FLOW_ID"})
public class FlowIDInputEventRoutingIntegrationTests extends BaseIntegrationTest {
	
	@Autowired private GenericEventPersisterConsumer persister;
	@Qualifier("TEST_FLOW_INPUT")
	@Autowired private PollableChannel flowInputChannel;
	
	@Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
	@Autowired private SourcePollingChannelAdapter pollableSource;
	
    @BeforeEach
    public void startSourcePolling() {
    	pollableSource.start();
    	
    	JsonUtils.resetObjectMapper();
    	JsonUtils.getObjectMapper().addMixIn(IsTypedJson.class, TestPayload.class);
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
    	pollableSource.stop();
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
    public static class TestPayload implements IsTypedJson {
    	
    	private Integer num;
    	private String str;
    }

}
