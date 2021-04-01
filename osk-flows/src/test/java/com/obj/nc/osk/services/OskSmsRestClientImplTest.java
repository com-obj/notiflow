package com.obj.nc.osk.services;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.osk.exception.SmsClientException;
import com.obj.nc.osk.functions.processors.sms.OskSmsSenderRestImpl;
import com.obj.nc.osk.functions.processors.sms.config.OskSmsSenderConfigProperties;
import com.obj.nc.osk.functions.processors.sms.dtos.OskSendSmsRequestDto;
import com.obj.nc.osk.functions.processors.sms.dtos.OskSendSmsResponseDto;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.ConstraintViolationException;

import static com.obj.nc.osk.functions.processors.sms.OskSmsSenderRestImpl.SEND_PATH;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ActiveProfiles(value = { "test"}, resolver = SystemPropertyActiveProfileResolver.class)
@RestClientTest(OskSmsSenderRestImpl.class)
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
@EnableConfigurationProperties(OskSmsSenderConfigProperties.class)
class OskSmsRestClientImplTest {

    @Autowired
    private OskSmsSenderRestImpl smsRestClient;

    @Autowired
    private OskSmsSenderConfigProperties properties;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Test
    void testCreateRequest() {
        // GIVEN
        String MESSAGE_PATH = "smsNotificationMessages/message.json";
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_PATH, Message.class);

        OskSendSmsRequestDto oskSendSmsRequestDto = smsRestClient.convertMessageToRequest(inputMessage);

        Assertions.assertThat(oskSendSmsRequestDto.getAddress()).hasSize(1);
        Assertions.assertThat(oskSendSmsRequestDto.getAddress().get(0)).isEqualTo(((SmsEndpoint) inputMessage.getBody().getRecievingEndpoints().get(0)).getPhone());
        Assertions.assertThat(oskSendSmsRequestDto.getSenderAddress()).isEqualTo(properties.getSenderAddress());
        Assertions.assertThat(oskSendSmsRequestDto.getBillCode()).isEqualTo(properties.getBillCode());

        SimpleTextContent contentTyped = inputMessage.getBody().getContentTyped();
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
        mockRestServerWithOneRequest(sendSmsRequest, sendSmsSuccessResponse);

        // WHEN
        OskSendSmsResponseDto sendSmsResponse = smsRestClient.sendRequest(sendSmsRequest);

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
        mockRestServerWithOneRequest(sendSmsRequest, sendSmsSuccessResponse);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.sendRequest(null))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void testSendSmsWithFailureResponse() {
        // GIVEN
        OskSendSmsRequestDto sendSmsRequest = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-request.json", OskSendSmsRequestDto.class);
        OskSendSmsResponseDto sendSmsFailureResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-failure.json", OskSendSmsResponseDto.class);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsRequest, sendSmsFailureResponse);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.sendRequest(sendSmsRequest))
                .isInstanceOf(SmsClientException.class)
                .hasMessageContaining(sendSmsFailureResponse.getResourceReference().getResourceURL());
    }

    @Test
    void testSendSmsWithInvalidResponse() {
        // GIVEN
        OskSendSmsRequestDto sendSmsRequest = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-request.json", OskSendSmsRequestDto.class);
        OskSendSmsResponseDto sendSmsInvalidResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-invalid.json", OskSendSmsResponseDto.class);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsRequest, sendSmsInvalidResponse);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.sendRequest(sendSmsRequest))
                .isInstanceOf(SmsClientException.class)
                .hasMessageContaining("Unknown response status");

        // GIVEN
        sendSmsInvalidResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-invalid.json", OskSendSmsResponseDto.class);
        sendSmsInvalidResponse.setResourceReference(null);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsRequest, sendSmsInvalidResponse);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.sendRequest(sendSmsRequest))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("Resource reference must not be null");

        // GIVEN
        sendSmsInvalidResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-invalid.json", OskSendSmsResponseDto.class);
        sendSmsInvalidResponse.getResourceReference().setResourceURL(null);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsRequest, sendSmsInvalidResponse);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.sendRequest(sendSmsRequest))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("Resource URL must not be null");
    }

    private void mockRestServerWithOneRequest(OskSendSmsRequestDto sendSmsRequest, OskSendSmsResponseDto sendSmsSuccessResponse) {
        mockRestServiceServer.reset();
        mockRestServiceServer.expect(ExpectedCount.once(),
                requestTo(UriComponentsBuilder.fromPath(SEND_PATH).build(sendSmsRequest.getSenderAddress())))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header("Authorization", "Basic dGVzdGxvZ2luOnRlc3Rwdw=="))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(sendSmsSuccessResponse))
                );
    }

}