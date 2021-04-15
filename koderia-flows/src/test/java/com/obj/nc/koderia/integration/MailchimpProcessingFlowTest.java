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
import com.obj.nc.functions.processors.senders.MailchimpSender;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersisterConsumer;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;
import com.obj.nc.koderia.KoderiaFlowsApplication;
import com.obj.nc.koderia.dto.koderia.recipients.RecipientDto;
import com.obj.nc.koderia.dto.koderia.event.BaseKoderiaEventDto;
import com.obj.nc.flows.testmode.mailchimp.dto.MessageResponseDto;
import com.obj.nc.koderia.functions.processors.mailchimpSender.MailchimpSenderConfig;
import com.obj.nc.koderia.functions.processors.mailchimpSender.MailchimpSenderProcessorFunction;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.MockIntegrationContext;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.koderia.flows.MaichimpProcessingFlowConfig.*;
import static com.obj.nc.koderia.functions.processors.mailchimpSender.MailchimpSenderConfig.MAILCHIMP_RESPONSE_FIELD;
import static com.obj.nc.koderia.functions.processors.mailchimpSender.MailchimpSenderConfig.SEND_TEMPLATE_PATH;
import static com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig.RECIPIENTS_PATH;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = LOG_CONSUMER_HANDLER_ID)
@SpringBootTest(properties = {
		"nc.flows.input-evet-routing.type=FLOW_ID",
		"spring.main.allow-bean-definition-overriding=true"
})
@DirtiesContext
public class MailchimpProcessingFlowTest extends BaseIntegrationTest {
	
	@Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
	@Autowired private SourcePollingChannelAdapter pollableSource;
	@Autowired private GenericEventPersisterConsumer persister;
	@Autowired private KoderiaRecipientsFinderProcessorFunction koderiaRecipientsFinder;
	@Autowired private MailchimpSenderProcessorFunction mailchimpSender;
	@Autowired private MailchimpSenderConfig mailchimpSenderConfig;
	@Autowired private KoderiaRecipientsFinderConfig koderiaRecipientsFinderConfig;
	@Autowired private MockIntegrationContext mockIntegrationContext;
	
	private MockRestServiceServer koderiaMockServer;
	private MockRestServiceServer mailchimpMockServer;
	
	private List<Message<?>> received = new ArrayList<>();
	
	@BeforeEach
	public void startSourcePollingAndMockRestServers() {
		koderiaMockServer = MockRestServiceServer.bindTo(koderiaRecipientsFinder.getRestTemplate()).build();
		mailchimpMockServer = MockRestServiceServer.bindTo(mailchimpSender.getRestTemplate()).build();
		pollableSource.start();
	}
	
	@AfterEach
	public void stopSourcePolling() {
		pollableSource.stop();
	}
	
	@Test
	void testSendMessagesToMailchimp() {
		// given
		BaseKoderiaEventDto baseKoderiaEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", BaseKoderiaEventDto.class);
		GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEventDto));
		genericEvent.setFlowId("default-flow");
		createRestCallExpectations();
		mockIntegrationContext.substituteMessageHandlerFor(LOG_CONSUMER_HANDLER_ID, received::add);
		// when
		persister.accept(genericEvent);
		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> received.size() >= 3);
		// then
		MatcherAssert.assertThat(received, Matchers.hasSize(3));
		// and
		for (Message<?> receivedMessage : received) {
			com.obj.nc.domain.message.Message payload = (com.obj.nc.domain.message.Message) receivedMessage.getPayload();
			checkReceivedPayload(payload);
		}
	}
	
	private void checkReceivedPayload(com.obj.nc.domain.message.Message payload) {
		String RESPONSE_JSON_PATH = "mailchimp/response_body.json";
		MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
		
		List<MessageResponseDto> messageResponses = (List<MessageResponseDto>) payload.getBody().getAttributeValue(MAILCHIMP_RESPONSE_FIELD);
		for (MessageResponseDto response : messageResponses) {
			MatcherAssert.assertThat(Arrays.asList(responseDtos), contains(response));
		}
	}
	
	private void createRestCallExpectations() {
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
		
		// mailchimp server
		String RESPONSE_JSON_PATH = "mailchimp/response_body.json";
		MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
		String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);
		
		mailchimpMockServer.expect(times(3), 
				requestTo(mailchimpSenderConfig.getMailchimpApi().getUrl() + SEND_TEMPLATE_PATH))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + mailchimpSenderConfig.getMailchimpApi().getAuthKey()))
				.andExpect(jsonPath("$.key", equalTo("MOCKkey")))
				.andExpect(jsonPath("$.message.subject", equalTo("Business Intelligence (BI) Developer")))
				.andExpect(jsonPath("$.message.merge_language", equalTo("handlebars")))
				.andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));
	}

}

