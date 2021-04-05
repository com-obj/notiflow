package com.obj.nc.koderia.integration;

import static com.obj.nc.koderia.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;
import static com.obj.nc.koderia.functions.processors.senders.MailchimpSenderExecution.MAILCHIMP_RESPONSE_FIELD;
import static com.obj.nc.koderia.services.MailchimpRestClientImpl.SEND_TEMPLATE_PATH;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Collections;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.koderia.KoderiaFlowsApplication;
import com.obj.nc.koderia.config.MailchimpApiConfig;
import com.obj.nc.koderia.dto.EmitEventDto;
import com.obj.nc.koderia.dto.EventDataDto;
import com.obj.nc.koderia.dto.mailchimp.MessageResponseDto;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@Import({
		TestChannelBinderConfiguration.class,
		KoderiaFlowTestsConfig.class
})
@AutoConfigureMockRestServiceServer
@ContextConfiguration(classes = KoderiaFlowsApplication.class)
@Disabled
public class KoderiaFlowTests extends BaseIntegrationTest {

	public static final String FINAL_STEP_QUEUE_NAME = "send-message.destination";

	@Autowired
	private MailchimpApiConfig mailchimpApiConfig;

	@Autowired
	private InputDestination source;

	@Autowired
	private OutputDestination target;

	@Autowired
	private CompositeMessageConverter messageConverter;

	@Autowired
	private MockRestServiceServer mockMailchimpRestServer;

	@Test
	public void testJobPostKoderiaEventEmited() throws Exception {
		// WITH MOCK SERVER CONFIG
		String mailchimpSendMessageUri = mailchimpApiConfig.getApi().getUri() + SEND_TEMPLATE_PATH;

		MessageResponseDto responseDto = new MessageResponseDto();
		responseDto.setId("string");
		responseDto.setEmail("user@example.com");
		responseDto.setRejectReason("hard_bounce");
		responseDto.setStatus("sent");

		List<MessageResponseDto> responseDtos = Collections.singletonList(responseDto);

		mockMailchimpRestServer.expect(times(3), requestTo(mailchimpSendMessageUri))
				.andRespond(withSuccess(JsonUtils.writeObjectToJSONString(responseDtos), MediaType.APPLICATION_JSON));

		// GIVEN
		String INPUT_JSON_FILE = "koderia/create_request/job_body.json";
		EmitEventDto emitEventDto = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmitEventDto.class);
		EventDataDto jobPostData = emitEventDto.getData();

		// WHEN
		GenericMessage<EmitEventDto> inputMessage = new GenericMessage<>(emitEventDto);
		source.send(inputMessage);

		org.springframework.messaging.Message<byte[]> payload1 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
		com.obj.nc.domain.message.Message message1 = (com.obj.nc.domain.message.Message) messageConverter.fromMessage(payload1, com.obj.nc.domain.message.Message.class);

		org.springframework.messaging.Message<byte[]> payload2 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
		com.obj.nc.domain.message.Message message2 = (com.obj.nc.domain.message.Message) messageConverter.fromMessage(payload2, com.obj.nc.domain.message.Message.class);

		org.springframework.messaging.Message<byte[]> payload3 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
		com.obj.nc.domain.message.Message message3 = (com.obj.nc.domain.message.Message) messageConverter.fromMessage(payload3, com.obj.nc.domain.message.Message.class);

		// THEN
		mockMailchimpRestServer.verify();

		MatcherAssert.assertThat(message1, CoreMatchers.notNullValue());
		EmailContent emailContent = message1.getContentTyped();
		MatcherAssert.assertThat(emailContent.getText(), Matchers.equalTo(jobPostData.getMessageText()));
		MatcherAssert.assertThat(emailContent.getSubject(), Matchers.equalTo(jobPostData.getMessageSubject()));
		MatcherAssert.assertThat(message1.getBody().getMessage().getAttributes().get(ORIGINAL_EVENT_FIELD), Matchers.equalTo(emitEventDto.asMap()));
		MatcherAssert.assertThat(message1.getBody().getAttributes().get(MAILCHIMP_RESPONSE_FIELD), Matchers.notNullValue());

		MatcherAssert.assertThat(message2, CoreMatchers.notNullValue());
		emailContent = message2.getContentTyped();
		MatcherAssert.assertThat(emailContent.getText(), Matchers.equalTo(jobPostData.getMessageText()));
		MatcherAssert.assertThat(emailContent.getSubject(), Matchers.equalTo(jobPostData.getMessageSubject()));
		MatcherAssert.assertThat(message2.getBody().getMessage().getAttributes().get(ORIGINAL_EVENT_FIELD), Matchers.equalTo(emitEventDto.asMap()));
		MatcherAssert.assertThat(message2.getBody().getAttributes().get(MAILCHIMP_RESPONSE_FIELD), Matchers.notNullValue());

		MatcherAssert.assertThat(message3, CoreMatchers.notNullValue());
		emailContent = message3.getContentTyped();
		MatcherAssert.assertThat(emailContent.getText(), Matchers.equalTo(jobPostData.getMessageText()));
		MatcherAssert.assertThat(emailContent.getSubject(), Matchers.equalTo(jobPostData.getMessageSubject()));
		MatcherAssert.assertThat(message3.getBody().getMessage().getAttributes().get(ORIGINAL_EVENT_FIELD), Matchers.equalTo(emitEventDto.asMap()));
		MatcherAssert.assertThat(message3.getBody().getAttributes().get(MAILCHIMP_RESPONSE_FIELD), Matchers.notNullValue());
	}

}
