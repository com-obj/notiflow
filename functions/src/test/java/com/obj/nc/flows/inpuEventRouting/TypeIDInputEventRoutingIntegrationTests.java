package com.obj.nc.flows.inpuEventRouting;

import javax.mail.MessagingException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersisterConsumer;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest
@SpringBootTest(properties = {
		"nc.flows.input-evet-routing.type=PAYLOAD_TYPE",
		"nc.flows.input-evet-routing.type-propery-name=@type",
		"nc.flows.input-evet-routing.type-channel-mapping.TYPE_1=CHANNEL_1",
		"nc.flows.input-evet-routing.type-channel-mapping.TYPE_2=CHANNEL_2"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS) //Because of correct disposal of green mail used for test mode
public class TypeIDInputEventRoutingIntegrationTests extends BaseIntegrationTest {
	
	@Autowired private GenericEventPersisterConsumer persister;
	
	@Qualifier("CHANNEL_1")
	@Autowired private PollableChannel flowInputChannel1;
	@Qualifier("CHANNEL_2")
	@Autowired private PollableChannel flowInputChannel2;
	
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

}
