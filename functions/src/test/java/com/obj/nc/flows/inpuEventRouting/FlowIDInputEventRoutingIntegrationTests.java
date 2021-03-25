package com.obj.nc.flows.inpuEventRouting;

import javax.mail.MessagingException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.test.context.SpringIntegrationTest;
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
		"nc.flows.input-evet-routing.type=FLOW_ID"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS) //Because of correct disposal of green mail used for test mode
public class FlowIDInputEventRoutingIntegrationTests extends BaseIntegrationTest {
	
	@Autowired private GenericEventPersisterConsumer persister;
	@Qualifier("TEST_FLOW_INPUT")
	@Autowired private PollableChannel flowInputChannel;
	
    @Test
    void testGenericEventRouting() throws MessagingException {
        GenericEvent event = GenericEvent.from(JsonUtils.readJsonNodeFromClassPathResource("events/generic_event.json"));
        event.setFlowId("TEST_FLOW");
        
        persister.accept(event);
        
	    Assertions.assertNotNull(flowInputChannel.receive(5000));
    }

    @TestConfiguration
    public static class TestModeTestConfiguration {

        @Bean("TEST_FLOW_INPUT")
        public PollableChannel flowInputChannel() {
            return new QueueChannel();
        }

    }

}
