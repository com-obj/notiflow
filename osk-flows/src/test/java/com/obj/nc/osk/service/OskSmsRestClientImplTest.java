package com.obj.nc.osk.service;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.osk.dto.OskSendSmsRequestDto;
import com.obj.nc.osk.dto.OskSendSmsResponseDto;
import com.obj.nc.osk.exception.SmsClientException;
import com.obj.nc.osk.functions.senders.OskSmsSenderConfigProperties;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ActiveProfiles(value = { "test"}, resolver = SystemPropertyActiveProfileResolver.class)
@RestClientTest(OskSmsRestClientImpl.class)
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
@EnableConfigurationProperties(OskSmsSenderConfigProperties.class)
class OskSmsRestClientImplTest {

    @Autowired
    private OskSmsRestClientImpl smsRestClient;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Test
    void testSendSmsWithSuccessResponse() {
        // GIVEN
        OskSendSmsRequestDto sendSmsRequest = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-request.json", OskSendSmsRequestDto.class);
        OskSendSmsResponseDto sendSmsSuccessResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-success.json", OskSendSmsResponseDto.class);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsRequest, sendSmsSuccessResponse);

        // WHEN
        OskSendSmsResponseDto sendSmsResponse = smsRestClient.send(sendSmsRequest);

        // THEN
        mockRestServiceServer.verify();
        Assertions.assertThat(sendSmsResponse).isEqualTo(sendSmsSuccessResponse);
    }

    @Test
    void testSendSmsWithInvalidRequest() {
        // GIVEN
        OskSendSmsRequestDto sendSmsRequest = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-request.json", OskSendSmsRequestDto.class);
        OskSendSmsResponseDto sendSmsSuccessResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-success.json", OskSendSmsResponseDto.class);
        sendSmsRequest.setSenderAddress(null);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsRequest, sendSmsSuccessResponse);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.send(sendSmsRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("must not be blank");

        mockRestServerWithOneRequest(sendSmsRequest, sendSmsSuccessResponse);
        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.send(null))
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
        Assertions.assertThatThrownBy(() -> smsRestClient.send(sendSmsRequest))
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
        Assertions.assertThatThrownBy(() -> smsRestClient.send(sendSmsRequest))
                .isInstanceOf(SmsClientException.class)
                .hasMessageContaining("Unknown response status");

        // GIVEN
        sendSmsInvalidResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-invalid.json", OskSendSmsResponseDto.class);
        sendSmsInvalidResponse.setResourceReference(null);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsRequest, sendSmsInvalidResponse);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.send(sendSmsRequest))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("Resource reference must not be null");

        // GIVEN
        sendSmsInvalidResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-invalid.json", OskSendSmsResponseDto.class);
        sendSmsInvalidResponse.getResourceReference().setResourceURL(null);

        // MOCK SERVER
        mockRestServerWithOneRequest(sendSmsRequest, sendSmsInvalidResponse);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.send(sendSmsRequest))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("Resource URL must not be null");
    }

    private void mockRestServerWithOneRequest(OskSendSmsRequestDto sendSmsRequest, OskSendSmsResponseDto sendSmsSuccessResponse) {
        mockRestServiceServer.reset();
        mockRestServiceServer.expect(ExpectedCount.once(),
                requestTo(UriComponentsBuilder.fromPath(SmsRestClientConstants.SEND_PATH).build(sendSmsRequest.getSenderAddress())))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header("Authorization", "Basic dGVzdGxvZ2luOnRlc3Rwdw=="))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(sendSmsSuccessResponse))
                );
    }

}