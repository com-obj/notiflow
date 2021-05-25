package com.obj.nc.koderia.functions.processors.mailchimpSender;

import static com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfig.MAILCHIMP_RESPONSE_FIELD;
import static com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfig.SEND_TEMPLATE_PATH;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.message.MailChimpMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderProcessorFunction;
import com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfig;
import com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfigProperties;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpSendTemplateResponse;
import com.obj.nc.koderia.config.DomainConfig;
import com.obj.nc.koderia.mapper.KoderiaMergeVarMapperImpl;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@RestClientTest(MailchimpSenderProcessorFunction.class)
@ContextConfiguration(classes = {
        MailchimpSenderConfig.class,
        KoderiaMergeVarMapperImpl.class,
        MailchimpSenderConfigProperties.class,
        DomainConfig.class
})
class MailchimpSenderProcessorFunctionTest {

    @Autowired private MailchimpSenderProcessorFunction sendMailchimpMessage;
    @Autowired private MailchimpSenderConfigProperties mailchimpSenderConfigProperties;
    @Autowired private MockRestServiceServer mailchimpMockServer;
    
    @BeforeEach
    void setUp() {
        createSimpleMessageRestServerExpectations();
    }
    
    @Test
    void testSendMessageWithTemplate() {
        // GIVEN
        MailChimpMessage inputMessage = JsonUtils.readObjectFromClassPathResource("mailchimp/message.json", MailChimpMessage.class);
        // WHEN
        MailChimpMessage outputMessage = sendMailchimpMessage.apply(inputMessage);
        // THEN
        MatcherAssert.assertThat(outputMessage, Matchers.notNullValue());
        MailchimpSendTemplateResponse outputMailchimpSendTemplateResponse = JsonUtils.readClassFromObject(outputMessage.getBody().getAttributeValueAs(MAILCHIMP_RESPONSE_FIELD, List.class).get(0), MailchimpSendTemplateResponse.class);
        MailchimpSendTemplateResponse[] responseDtos = JsonUtils.readObjectFromClassPathResource("mailchimp/response_body.json", MailchimpSendTemplateResponse[].class);
        MatcherAssert.assertThat(outputMailchimpSendTemplateResponse, Matchers.equalTo(responseDtos[0]));
    }

    @Test
    void testSendNullMessage() {
        // GIVEN
    	MailChimpMessage inputMessage = null;
        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Message must not be null");
    }

    @Test
    void testSendMessageWithNoReceivingEndpoints() {
        // GIVEN
    	MailChimpMessage inputMessage = JsonUtils.readObjectFromClassPathResource("mailchimp/message.json", MailChimpMessage.class);
        inputMessage.setRecievingEndpoints(new ArrayList<>());

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("MailchimpSender can only send message")
                .hasMessageContaining("to 1 MailchimpEndpoint");
    }

    @Test
    void testSendMessageWithNonEmailReceivingEndpoints() {
        // GIVEN
    	MailChimpMessage inputMessage = JsonUtils.readObjectFromClassPathResource("mailchimp/message.json", MailChimpMessage.class);
        inputMessage.setRecievingEndpoints(Arrays.asList(
                new MailchimpEndpoint() {
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
        MailchimpSendTemplateResponse[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MailchimpSendTemplateResponse[].class);
        String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);
        
        mailchimpMockServer.expect(times(1),
                requestTo(SEND_TEMPLATE_PATH))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + mailchimpSenderConfigProperties.getAuthKey()))
                .andExpect(jsonPath("$.key", equalTo("mockAuthKey")))
                .andExpect(jsonPath("$.message.subject", anyOf(equalTo("Business Intelligence (BI) Developer 1, 400 – 500 € / manday, Viedeň"))))
                .andExpect(jsonPath("$.message.merge_language", equalTo("handlebars")))
                .andExpect(jsonPath("$.message.attachments[0].name", equalTo("test1.txt")))
                .andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));
    }
    
}