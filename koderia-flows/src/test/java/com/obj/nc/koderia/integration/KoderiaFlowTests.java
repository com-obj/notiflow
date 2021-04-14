package com.obj.nc.koderia.integration;

import static com.obj.nc.koderia.functions.processors.mailchimpSender.MailchimpSenderConfig.SEND_TEMPLATE_PATH;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Collections;
import java.util.List;

import com.obj.nc.config.InjectorConfiguration;
import com.obj.nc.koderia.KoderiaFlowsApplication;
import com.obj.nc.koderia.functions.processors.mailchimpSender.MailchimpSenderConfig;
import com.obj.nc.koderia.functions.processors.mailchimpSender.MailchimpSenderProcessorFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.koderia.dto.koderia.event.BaseKoderiaEventDto;
import com.obj.nc.koderia.dto.mailchimp.MessageResponseDto;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(classes = { KoderiaFlowsApplication.class, InjectorConfiguration.class })
@Disabled // will transition from cloud function to integration flow 
public class KoderiaFlowTests extends BaseIntegrationTest {
	
	@Autowired private MailchimpSenderConfig mailchimpSenderConfig;
	@Autowired private MailchimpSenderProcessorFunction mailchimpServiceRest;
	private MockRestServiceServer mockMailchimpRestServer;

	@BeforeEach
	void redirectRestTemplate() {
		mockMailchimpRestServer = MockRestServiceServer.bindTo(mailchimpServiceRest.getRestTemplate()).build();
	}
	
	@Test
	public void testJobPostKoderiaEventEmited() {
		// WITH MOCK SERVER CONFIG
		String mailchimpSendMessageUrl = mailchimpSenderConfig.getMailchimpApi().getUrl() + SEND_TEMPLATE_PATH;

		MessageResponseDto responseDto = new MessageResponseDto();
		responseDto.setId("string");
		responseDto.setEmail("user@example.com");
		responseDto.setRejectReason("hard_bounce");
		responseDto.setStatus("sent");

		List<MessageResponseDto> responseDtos = Collections.singletonList(responseDto);

		mockMailchimpRestServer.expect(times(3), requestTo(mailchimpSendMessageUrl))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess(JsonUtils.writeObjectToJSONString(responseDtos), MediaType.APPLICATION_JSON));

		// GIVEN
		String INPUT_JSON_FILE = "koderia/create_request/job_body.json";
		BaseKoderiaEventDto baseKoderiaEventDto = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, BaseKoderiaEventDto.class);
		BaseKoderiaEventDto jobPostData = baseKoderiaEventDto.getData();

		// WHEN
		GenericMessage<BaseKoderiaEventDto> inputMessage = new GenericMessage<>(baseKoderiaEventDto);

		// THEN
/*		mockMailchimpRestServer.verify();

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
		MatcherAssert.assertThat(message3.getBody().getAttributes().get(MAILCHIMP_RESPONSE_FIELD), Matchers.notNullValue());*/
	}

}

