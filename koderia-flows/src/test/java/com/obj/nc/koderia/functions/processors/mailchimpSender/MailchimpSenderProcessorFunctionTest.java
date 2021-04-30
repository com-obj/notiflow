package com.obj.nc.koderia.functions.processors.mailchimpSender;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderProcessorFunction;
import com.obj.nc.functions.processors.senders.mailchimp.model.MailchimpResponseDto;
import com.obj.nc.koderia.functions.processors.eventConverter.KoderiaEventConverterConfig;
import com.obj.nc.mappers.MailchimpDataToMailchimpContentMapper;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig.MAILCHIMP_RESPONSE_FIELD;
import static com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig.SEND_TEMPLATE_PATH;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@RestClientTest(MailchimpSenderProcessorFunction.class)
@ContextConfiguration(classes = {
        MailchimpSenderConfig.class,
        MailchimpDataToMailchimpContentMapper.class,
        MailchimpSenderConfigProperties.class
})
class MailchimpSenderProcessorFunctionTest {

    @Autowired private MailchimpSenderProcessorFunction sendMailchimpMessage;
    @Autowired private MailchimpSenderConfigProperties mailchimpSenderConfigProperties;
    @Autowired private MockRestServiceServer mailchimpMockServer;
    
    @BeforeEach
    void setUp(TestInfo testInfo) {
        if(testInfo.getTags().contains("aggregate")) {
            createAggregateMessageRestServerExpectations();
        } else {
            createSimpleMessageRestServerExpectations();
        }
    }
    
    @Test
    void testSendMessageWithTemplate() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource("mailchimp/message.json", Message.class);
        // WHEN
        Message outputMessage = sendMailchimpMessage.apply(inputMessage);
        // THEN
        MatcherAssert.assertThat(outputMessage, Matchers.notNullValue());
        MailchimpResponseDto outputMailchimpResponseDto = JsonUtils.readClassFromObject(outputMessage.getBody().getAttributeValueAs(MAILCHIMP_RESPONSE_FIELD, List.class).get(0), MailchimpResponseDto.class);
        MailchimpResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource("mailchimp/response_body.json", MailchimpResponseDto[].class);
        MatcherAssert.assertThat(outputMailchimpResponseDto, Matchers.equalTo(responseDtos[0]));
    }

    @Test
    void testSendNullMessage() {
        // GIVEN
        Message inputMessage = null;
        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Message must not be null");
    }

    @Test
    void testSendMessageWithNoReceivingEndpoints() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource("mailchimp/message.json", Message.class);
        inputMessage.getBody().setRecievingEndpoints(new ArrayList<>());

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("MailchimpSender can only send message")
                .hasMessageContaining("to 1 MailchimpEndpoint");
    }

    @Test
    void testSendMessageWithNonEmailReceivingEndpoints() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource("mailchimp/message.json", Message.class);
        inputMessage.getBody().setRecievingEndpoints(Arrays.asList(
                new RecievingEndpoint() {
                    @Override
                    public String getEndpointId() {
                        return "TEST";
                    }

                    @Override
                    public String getEndpointType() {
                        return "TEST";
                    }

					@Override
					public void setEndpointId(String endpointId) {
					}
                }
        ));

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("MailchimpSender can only send message")
                .hasMessageContaining("to 1 MailchimpEndpoint");
    }
    
    
    private void createSimpleMessageRestServerExpectations() {
        String RESPONSE_JSON_PATH = "mailchimp/response_body.json";
        MailchimpResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MailchimpResponseDto[].class);
        String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);
        
        mailchimpMockServer.expect(times(1),
                requestTo(SEND_TEMPLATE_PATH))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + mailchimpSenderConfigProperties.getAuthKey()))
                .andExpect(jsonPath("$.key", equalTo("MOCKkey")))
                .andExpect(jsonPath("$.message.subject", anyOf(equalTo("Business Intelligence (BI) Developer"))))
                .andExpect(jsonPath("$.message.merge_language", equalTo("handlebars")))
                .andExpect(jsonPath("$.message.attachments[0].name", equalTo("test1.txt")))
                .andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));
    }
    
    private void createAggregateMessageRestServerExpectations() {
        String RESPONSE_JSON_PATH = "mailchimp/response_body.json";
        MailchimpResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MailchimpResponseDto[].class);
        String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);
        
        mailchimpMockServer.expect(times(1),
                requestTo(SEND_TEMPLATE_PATH))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + mailchimpSenderConfigProperties.getAuthKey()))
                .andExpect(jsonPath("$.key", equalTo("MOCKkey")))
                .andExpect(jsonPath("$.message.subject", equalTo("Koderia digest")))
                .andExpect(jsonPath("$.message.merge_language", equalTo("handlebars")))
                .andExpect(jsonPath("$.message.global_merge_vars[0].name", anyOf(equalTo("JOB_POST"), equalTo("NEWS"),
                        equalTo("LINK"), equalTo("EVENT"), equalTo("BLOG"))))
                .andExpect(jsonPath("$.message.global_merge_vars[1].name", anyOf(equalTo("JOB_POST"), equalTo("NEWS"),
                        equalTo("LINK"), equalTo("EVENT"), equalTo("BLOG"))))
                .andExpect(jsonPath("$.message.global_merge_vars[2].name", anyOf(equalTo("JOB_POST"), equalTo("NEWS"),
                        equalTo("LINK"), equalTo("EVENT"), equalTo("BLOG"))))
                .andExpect(jsonPath("$.message.global_merge_vars[3].name", anyOf(equalTo("JOB_POST"), equalTo("NEWS"),
                        equalTo("LINK"), equalTo("EVENT"), equalTo("BLOG"))))
                .andExpect(jsonPath("$.message.global_merge_vars[4].name", anyOf(equalTo("JOB_POST"), equalTo("NEWS"),
                        equalTo("LINK"), equalTo("EVENT"), equalTo("BLOG"))))
                .andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));
    }
    
}