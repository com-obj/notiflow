package com.obj.nc.osk.service;

import com.obj.nc.osk.dto.SendSmsRequestDto;
import com.obj.nc.osk.dto.SendSmsResponseDto;
import com.obj.nc.osk.exception.SmsClientException;
import com.obj.nc.osk.functions.senders.SmsSenderConfigProperties;
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
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RestClientTest(SmsRestClientImpl.class)
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
@EnableConfigurationProperties(SmsSenderConfigProperties.class)
class SmsRestClientImplTest {

    @Autowired
    private SmsRestClientImpl smsRestClient;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Test
    void testSendSmsWithSuccessResponse() {
        // GIVEN
        SendSmsRequestDto sendSmsRequest = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-request.json", SendSmsRequestDto.class);
        SendSmsResponseDto sendSmsSuccessResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-success.json", SendSmsResponseDto.class);

        // MOCK SERVER
        mockRestServiceServer.expect(ExpectedCount.once(),
                requestTo(UriComponentsBuilder.fromPath(SmsRestClientConstants.SEND_PATH).build(sendSmsRequest.getSenderAddress())))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(sendSmsSuccessResponse))
                );

        // WHEN
        SendSmsResponseDto sendSmsResponse = smsRestClient.sendSms(sendSmsRequest);

        // THEN
        mockRestServiceServer.verify();
        Assertions.assertThat(sendSmsResponse).isEqualTo(sendSmsSuccessResponse);
    }

    @Test
    void testSendSmsWithInvalidRequest() {
        // GIVEN
        SendSmsRequestDto sendSmsRequest = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-request.json", SendSmsRequestDto.class);
        sendSmsRequest.setSenderAddress(null);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.sendSms(sendSmsRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("must not be blank");

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.sendSms(null))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void testSendSmsWithFailureResponse() {
        // GIVEN
        SendSmsRequestDto sendSmsRequest = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-request.json", SendSmsRequestDto.class);
        SendSmsResponseDto sendSmsFailureResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-failure.json", SendSmsResponseDto.class);

        // MOCK SERVER
        mockRestServiceServer.expect(ExpectedCount.once(),
                requestTo(UriComponentsBuilder.fromPath(SmsRestClientConstants.SEND_PATH).build(sendSmsRequest.getSenderAddress())))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(sendSmsFailureResponse))
                );

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.sendSms(sendSmsRequest))
                .isInstanceOf(SmsClientException.class)
                .hasMessageContaining(sendSmsFailureResponse.getResourceReference().getResourceURL());
    }

    @Test
    void testSendSmsWithInvalidResponse() {
        // GIVEN
        SendSmsRequestDto sendSmsRequest = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-request.json", SendSmsRequestDto.class);
        SendSmsResponseDto sendSmsInvalidResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-invalid.json", SendSmsResponseDto.class);

        // MOCK SERVER
        mockRestServiceServer.expect(ExpectedCount.once(),
                requestTo(UriComponentsBuilder.fromPath(SmsRestClientConstants.SEND_PATH).build(sendSmsRequest.getSenderAddress())))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(sendSmsInvalidResponse))
                );

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.sendSms(sendSmsRequest))
                .isInstanceOf(SmsClientException.class)
                .hasMessageContaining("Unknown response status");

        // GIVEN
        sendSmsInvalidResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-invalid.json", SendSmsResponseDto.class);
        sendSmsInvalidResponse.setResourceReference(null);

        // MOCK SERVER
        mockRestServiceServer.reset();
        mockRestServiceServer.expect(ExpectedCount.once(),
                requestTo(UriComponentsBuilder.fromPath(SmsRestClientConstants.SEND_PATH).build(sendSmsRequest.getSenderAddress())))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(sendSmsInvalidResponse))
                );

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.sendSms(sendSmsRequest))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("Resource reference must not be null");

        // GIVEN
        sendSmsInvalidResponse = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-invalid.json", SendSmsResponseDto.class);
        sendSmsInvalidResponse.getResourceReference().setResourceURL(null);

        // MOCK SERVER
        mockRestServiceServer.reset();
        mockRestServiceServer.expect(ExpectedCount.once(),
                requestTo(UriComponentsBuilder.fromPath(SmsRestClientConstants.SEND_PATH).build(sendSmsRequest.getSenderAddress())))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(sendSmsInvalidResponse))
                );

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> smsRestClient.sendSms(sendSmsRequest))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("Resource URL must not be null");
    }

}