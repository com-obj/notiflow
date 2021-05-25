package com.obj.nc.koderia.integration;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.flows.intenToMessageToSender.NotificationIntentProcessingFlowConfig.INTENT_PROCESSING_FLOW_ID;
import static com.obj.nc.flows.intenToMessageToSender.NotificationIntentProcessingFlowConfig.INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig.RECIPIENTS_PATH;
import static com.obj.nc.koderia.integration.CovertKoderiaEventFlowTest.MockNextFlowTestConfiguration.RECEIVED_TEST_LIST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersisterConsumer;
import com.obj.nc.koderia.domain.event.JobPostKoderiaEventDto;
import com.obj.nc.koderia.domain.recipients.RecipientDto;
import com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinder;
import com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest(properties = {
			"nc.flows.input-evet-routing.type=FLOW_ID", 
			"spring.main.allow-bean-definition-overriding=true"
})
@DirtiesContext
public class CovertKoderiaEventFlowTest extends BaseIntegrationTest {
	
	@Autowired private KoderiaRecipientsFinder koderiaRecipientsFinder;
	@Autowired private KoderiaRecipientsFinderConfig koderiaRecipientsFinderConfig;
	@Autowired private GenericEventPersisterConsumer persister;
	@Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
	@Autowired private SourcePollingChannelAdapter pollableSource;
	@Qualifier(RECEIVED_TEST_LIST)
	@Autowired private List<Message<?>> received;
	@Autowired private JdbcTemplate jdbcTemplate;
	private MockRestServiceServer koderiaMockServer;
	
	@BeforeEach
	public void startSourcePolling() {
		purgeNotifTables(jdbcTemplate);
		koderiaMockServer = MockRestServiceServer.bindTo(koderiaRecipientsFinder.getRestTemplate()).build();
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
		createRestCallExpectation();
		// when
		persister.accept(genericEvent);
		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> received.size() >= 1);
		// then
		assertThat(received, Matchers.hasSize(1));
		// and
		NotificationIntent<MailchimpContent> payload = (NotificationIntent<MailchimpContent>) received.get(0).getPayload();
		MailchimpContent content = payload.getBody();
		assertThat(content, notNullValue());
		
		MailchimpData data = content.getOriginalEvent();
		assertThat(data, notNullValue());
		assertThat(data.getType(), equalTo(baseKoderiaEvent.getType()));
		assertThat(data.getData(), equalTo(baseKoderiaEvent.getData()));
		
		assertThat(content.getTemplateContent(), notNullValue());
		assertThat(content.getTemplateName(), notNullValue());
	}
	
	private void createRestCallExpectation() {
		// koderia server
		String RECIPIENTS_JSON_PATH = "koderia/recipient_queries/job_recipients_response.json";
		RecipientDto[] responseBody = JsonUtils.readObjectFromClassPathResource(RECIPIENTS_JSON_PATH, RecipientDto[].class);
		
		koderiaMockServer.expect(ExpectedCount.once(),
				requestTo(koderiaRecipientsFinderConfig.getKoderiaApiUrl() + RECIPIENTS_PATH))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + koderiaRecipientsFinderConfig.getKoderiaApiToken()))
				.andExpect(jsonPath("$.type", equalTo("JOB_POST")))
				.andExpect(jsonPath("$.data.type", equalTo("Analytik")))
				.andExpect(jsonPath("$.data.technologies[0]", equalTo("Microsoft Power BI")))
				.andRespond(withStatus(HttpStatus.OK)
						.contentType(MediaType.APPLICATION_JSON)
						.body(JsonUtils.writeObjectToJSONString(responseBody))
				);
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

