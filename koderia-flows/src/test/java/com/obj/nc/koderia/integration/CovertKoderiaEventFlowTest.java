package com.obj.nc.koderia.integration;

import static com.obj.nc.flows.config.NotificationIntentProcessingFlowConfig.INTENT_PROCESSING_FLOW_ID;
import static com.obj.nc.flows.config.NotificationIntentProcessingFlowConfig.INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.koderia.flows.ConvertKoderiaEventFlowConfig.CONVERT_KODERIA_EVENT_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.koderia.flows.MaichimpProcessingFlowConfig.MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static org.springframework.integration.scheduling.PollerMetadata.DEFAULT_POLLER;

import com.obj.nc.config.InjectorConfiguration;
import com.obj.nc.config.JdbcConfiguration;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig;
import com.obj.nc.flows.inputEventRouting.config.InputEventRoutingProperties;
import com.obj.nc.functions.processors.eventFactory.GenericEventToNotificaitonIntentConverter;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersisterConsumer;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;
import com.obj.nc.koderia.KoderiaFlowsApplication;
import com.obj.nc.koderia.dto.koderia.event.BaseKoderiaEventDto;
import com.obj.nc.utils.JsonUtils;
import org.awaitility.Awaitility;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.PollerSpec;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.MockIntegrationContext;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(
	properties = {
			"nc.flows.input-evet-routing.type=FLOW_ID", 
			"spring.main.allow-bean-definition-overriding=true"
	}, classes = {
			KoderiaFlowsApplication.class, 
			InputEventRoutingFlowConfig.class, 
			InjectorConfiguration.class, 
			InputEventRoutingProperties.class,
			JdbcConfiguration.class,
			GenericEventToNotificaitonIntentConverter.class,
			PaylaodLoggerSinkConsumer.class,
			ValidateAndGenerateEventIdProcessingFunction.class,
			MessagesFromNotificationIntentProcessingFunction.class,
			CovertKoderiaEventFlowTest.TestChannelConfiguration.class,
			GenericEventPersisterConsumer.class
})
@DirtiesContext
public class CovertKoderiaEventFlowTest extends BaseIntegrationTest {
	
	@Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
	@Autowired private SourcePollingChannelAdapter pollableSource;
	@Autowired private GenericEventPersisterConsumer persister;
	static final List<Message<?>> received = new ArrayList<>();
	
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
		BaseKoderiaEventDto baseKoderiaEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", BaseKoderiaEventDto.class);
		GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEventDto));
		genericEvent.setFlowId("default-flow");
		persister.accept(genericEvent);
		
		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> received.size() >= 1);
		
		MatcherAssert.assertThat(received, Matchers.hasSize(1));
	}
	
	@TestConfiguration
	public static class TestChannelConfiguration {
		@Bean(INTENT_PROCESSING_FLOW_ID)
		public IntegrationFlow intentProcessingFlowDefinition() {
			return IntegrationFlows
					.from(INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID)
					.handle((MessageHandler) received::add)
					.get();
		}
	}

}

