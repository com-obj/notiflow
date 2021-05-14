package com.obj.nc.koderia.integration;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersisterConsumer;
import com.obj.nc.koderia.domain.event.JobPostKoderiaEventDto;
import com.obj.nc.utils.JsonUtils;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.flows.intenToMessageToSender.NotificationIntentProcessingFlowConfig.INTENT_PROCESSING_FLOW_ID;
import static com.obj.nc.flows.intenToMessageToSender.NotificationIntentProcessingFlowConfig.INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.koderia.integration.CovertKoderiaEventFlowTest.MockNextFlowTestConfiguration.RECEIVED_TEST_LIST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest(properties = {
			"nc.flows.input-evet-routing.type=FLOW_ID", 
			"spring.main.allow-bean-definition-overriding=true"
})
@DirtiesContext
public class CovertKoderiaEventFlowTest extends BaseIntegrationTest {
	
	@Autowired private GenericEventPersisterConsumer persister;
	@Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
	@Autowired private SourcePollingChannelAdapter pollableSource;
	@Qualifier(RECEIVED_TEST_LIST)
	@Autowired private List<Message<?>> received;
	
	@BeforeEach
	public void startSourcePolling() {
		pollableSource.start();
	}
	
	@AfterEach
	public void stopSourcePolling() {
		pollableSource.stop();
	}
	
	@Test
	void testConvertKoderiaEvent() {
		// given
		JobPostKoderiaEventDto baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", JobPostKoderiaEventDto.class);
		GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));
		genericEvent.setFlowId("default-flow");
		// when
		persister.accept(genericEvent);
		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> received.size() >= 1);
		// then
		assertThat(received, Matchers.hasSize(1));
		// and
		NotificationIntent payload = (NotificationIntent) received.get(0).getPayload();
		MailchimpContent content = payload.getContentTyped();
		assertThat(content.getMessage(), notNullValue());
		
		MailchimpData data = content.getMessage().getOriginalEvent();
		assertThat(data, notNullValue());
		assertThat(data.getType(), equalTo(baseKoderiaEvent.getType()));
		assertThat(data.getData(), equalTo(baseKoderiaEvent.getData()));
		
		assertThat(content.getTemplateContent(), notNullValue());
		assertThat(content.getTemplateName(), notNullValue());
	}
	
	@TestConfiguration
	public static class MockNextFlowTestConfiguration {
		public static final String RECEIVED_TEST_LIST = "RECEIVED_TEST_LIST";
		
		@Bean(RECEIVED_TEST_LIST)
		public List<Message<?>> received() {
			return new ArrayList<>();
		}
		
		@Bean(INTENT_PROCESSING_FLOW_ID)
		public IntegrationFlow intentProcessingFlowDefinition() {
			return IntegrationFlows
					.from(INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID)
					.handle((MessageHandler) received()::add)
					.get();
		}
	}

}

