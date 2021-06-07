package com.obj.nc.osk.functions.processors.sms;

import static com.obj.nc.osk.functions.processors.sms.OskSmsSenderRestImpl.SEND_PATH;
import static com.obj.nc.osk.functions.processors.sms.OskSmsSenderRestImpl.SEND_SMS_RESPONSE_ATTRIBUTE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import javax.validation.ConstraintViolationException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.config.InjectorConfiguration;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SmstMessage;
import com.obj.nc.osk.exception.SmsClientException;
import com.obj.nc.osk.functions.processors.sms.config.OskSmsSenderConfig;
import com.obj.nc.osk.functions.processors.sms.config.OskSmsSenderConfigProperties;
import com.obj.nc.osk.functions.processors.sms.dtos.OskSendSmsRequestDto;
import com.obj.nc.osk.functions.processors.sms.dtos.OskSendSmsResponseDto;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
@RestClientTest(OskSmsSenderRestImpl.class)
@ContextConfiguration(classes = {
		OskSmsSenderConfig.class,
		OskSmsSenderConfigProperties.class,
		InjectorConfiguration.class
})
class OskSmsSenderTest extends BaseIntegrationTest {

    
	@Autowired private OskSmsSenderRestImpl smsSender;
	@Autowired private OskSmsSenderConfigProperties properties;
	private MockRestServiceServer mockRestServiceServer;
    
    @BeforeEach
    public void init() {
    	mockRestServiceServer = MockRestServiceServer.createServer(smsSender.getRestTemplate());
    }

    @Test
    void testSendSms() {
        // GIVEN
        String MESSAGE_PATH = "smsNotificationMessages/message.json";
        SmstMessage inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_PATH, SmstMessage.class);
        
        // MOCK SERVER
        OskSendSmsResponseDto sendSmsResponseExpected = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-success.json", OskSendSmsResponseDto.class);

        mockRestServerWithOneRequest(sendSmsResponseExpected);

        // WHEN
        Message<SimpleTextContent> sentMessage = smsSender.apply(inputMessage);

        // THEN
        mockRestServiceServer.verify();
        Assertions.assertThat(sentMessage.getBody().containsAttribute(SEND_SMS_RESPONSE_ATTRIBUTE)).isTrue();
        OskSendSmsResponseDto sendSmsResponseActual = sentMessage.getBody().getAttributeValueAs(SEND_SMS_RESPONSE_ATTRIBUTE, OskSendSmsResponseDto.class);
        Assertions.assertThat(sendSmsResponseActual).isEqualTo(sendSmsResponseExpected);
    }
    
    @Test
    void testCreateRequest() {
        // GIVEN
        String MESSAGE_PATH = "smsNotificationMessages/message.json";
        SmstMessage inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_PATH, SmstMessage.class);

        OskSendSmsRequestDto oskSendSmsRequestDto = smsSender.convertMessageToRequest(inputMessage);

        Assertions.assertThat(oskSendSmsRequestDto.getAddress()).hasSize(1);
        Assertions.assertThat(oskSendSmsRequestDto.getAddress().get(0)).isEqualTo(((SmsEndpoint) inputMessage.getRecievingEndpoints().get(0)).getPhone());
        Assertions.assertThat(oskSendSmsRequestDto.getSenderAddress()).isEqualTo(properties.getSenderAddress());
        Assertions.assertThat(oskSendSmsRequestDto.getBillCode()).isEqualTo(properties.getBillCode());

        SimpleTextContent contentTyped = inputMessage.getBody();
        Assertions.assertThat(oskSendSmsRequestDto.getMessage()).isEqualTo(contentTyped.getText());
        Assertions.assertThat(oskSendSmsRequestDto.getClientCorrelator()).contains(properties.getClientCorrelatorPrefix());
        Assertions.assertThat(oskSendSmsRequestDto.getNotifyURL()).isEqualTo(properties.getNotifyUrl());
    }

    @Test
    void testSendSmsWithSuccessResponse() {
        // GIVEN
        OskSendSmsRequestDto sendSmsRequest = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-request.json", OskSendSmsRequestDto.class);
        OskSendSmsResponseDto sendSmsSuccessResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-success.json", OskSendSmsResponseDto.class);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsSuccessResponse);

        // WHEN
        OskSendSmsResponseDto sendSmsResponse = smsSender.sendRequest(sendSmsRequest);

        // THEN
        mockRestServiceServer.verify();
        Assertions.assertThat(sendSmsResponse).isEqualTo(sendSmsSuccessResponse);
    }

    @Test
    void testSendSmsWithInvalidRequestFailsOnValidation() {
        // GIVEN
        OskSendSmsRequestDto sendSmsRequest = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-request.json", OskSendSmsRequestDto.class);
        OskSendSmsResponseDto sendSmsSuccessResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-success.json", OskSendSmsResponseDto.class);
        sendSmsRequest.setSenderAddress(null);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsSuccessResponse);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsSender.sendRequest(null))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void testSendSmsWithFailureResponse() {
        // GIVEN
        OskSendSmsRequestDto sendSmsRequest = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-request.json", OskSendSmsRequestDto.class);
        OskSendSmsResponseDto sendSmsFailureResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-failure.json", OskSendSmsResponseDto.class);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsFailureResponse);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsSender.sendRequest(sendSmsRequest))
                .isInstanceOf(SmsClientException.class)
                .hasMessageContaining(sendSmsFailureResponse.getResourceReference().getResourceURL());
    }

    @Test
    void testSendSmsWithInvalidResponse() {
        // GIVEN
        OskSendSmsRequestDto sendSmsRequest = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-request.json", OskSendSmsRequestDto.class);
        OskSendSmsResponseDto sendSmsInvalidResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-invalid.json", OskSendSmsResponseDto.class);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsInvalidResponse);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsSender.sendRequest(sendSmsRequest))
                .isInstanceOf(SmsClientException.class)
                .hasMessageContaining("Unknown response status");

        // GIVEN
        sendSmsInvalidResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-invalid.json", OskSendSmsResponseDto.class);
        sendSmsInvalidResponse.setResourceReference(null);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsInvalidResponse);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsSender.sendRequest(sendSmsRequest))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("Resource reference must not be null");

        // GIVEN
        sendSmsInvalidResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-invalid.json", OskSendSmsResponseDto.class);
        sendSmsInvalidResponse.getResourceReference().setResourceURL(null);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsInvalidResponse);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsSender.sendRequest(sendSmsRequest))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("Resource URL must not be null");
    }

    private void mockRestServerWithOneRequest(OskSendSmsResponseDto sendSmsSuccessResponse) {
        mockRestServiceServer.reset();
        mockRestServiceServer.expect( 
        			ExpectedCount.once(),
        			requestTo(UriComponentsBuilder.fromHttpUrl(properties.getGapApiUrl() + SEND_PATH).build(properties.getSenderAddress()))
                )
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header("Authorization", "Basic dGVzdGxvZ2luOnRlc3Rwdw=="))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(sendSmsSuccessResponse))
                );
    }


}