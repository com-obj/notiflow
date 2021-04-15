package com.obj.nc.koderia.integration;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
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
import com.obj.nc.koderia.dto.koderia.data.RecipientDto;
import com.obj.nc.koderia.dto.koderia.event.BaseKoderiaEventDto;
import com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig;
import com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderProcessorFunction;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.PollerSpec;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.koderia.flows.ConvertKoderiaEventFlowConfig.CONVERT_KODERIA_EVENT_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.koderia.flows.MaichimpProcessingFlowConfig.MAILCHIMP_PROCESSING_FLOW_ID;
import static com.obj.nc.koderia.flows.MaichimpProcessingFlowConfig.MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig.RECIPIENTS_PATH;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.integration.scheduling.PollerMetadata.DEFAULT_POLLER;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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
			NotificationIntentProcessingFlowTest.MockMailChimpProcessingFlowTestConfiguration.class,
			GenericEventPersisterConsumer.class
})
@DirtiesContext
public class NotificationIntentProcessingFlowTest extends BaseIntegrationTest {
	
	@Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
	@Autowired private SourcePollingChannelAdapter pollableSource;
	@Autowired private GenericEventPersisterConsumer persister;
	@Autowired private KoderiaRecipientsFinderProcessorFunction koderiaRecipientsFinder;
	@Autowired private KoderiaRecipientsFinderConfig koderiaRecipientsFinderConfig;
	private MockRestServiceServer mockServer;
	static final List<Message<?>> received = new ArrayList<>();
	
	@BeforeEach
	public void startSourcePollingAndMockRestServer() {
		mockServer = MockRestServiceServer.bindTo(koderiaRecipientsFinder.getRestTemplate()).build();
		pollableSource.start();
	}
	
	@AfterEach
	public void stopSourcePolling() {
		pollableSource.stop();
	}
	
	@Test
	void testProcessNotificationIntent() {
		BaseKoderiaEventDto baseKoderiaEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", BaseKoderiaEventDto.class);
		GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEventDto));
		genericEvent.setFlowId("default-flow");
		persister.accept(genericEvent);
		createRestCallExpectation();
		
		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> received.size() >= 3);
		
		MatcherAssert.assertThat(received, Matchers.hasSize(3));
	}
	
	private void createRestCallExpectation() {
		String RECIPIENTS_JSON_PATH = "koderia/recipient_queries/job_recipients_response.json";
		RecipientDto[] responseBody = JsonUtils.readObjectFromClassPathResource(RECIPIENTS_JSON_PATH, RecipientDto[].class);
		
		mockServer.expect(ExpectedCount.once(),
				requestTo(koderiaRecipientsFinderConfig.getKoderiaApiUrl() + RECIPIENTS_PATH))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + koderiaRecipientsFinderConfig.getKoderiaApiToken()))
				.andRespond(withStatus(HttpStatus.OK)
						.contentType(MediaType.APPLICATION_JSON)
						.body(JsonUtils.writeObjectToJSONString(responseBody))
				);
	}
	
	@TestConfiguration
	public static class MockMailChimpProcessingFlowTestConfiguration {
		@Bean(MAILCHIMP_PROCESSING_FLOW_ID)
		public IntegrationFlow intentProcessingFlowDefinition() {
			return IntegrationFlows
					.from(MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID)
					.handle((MessageHandler) received::add)
					.get();
		}
	}

}

