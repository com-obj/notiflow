package com.obj.nc.osk.senders;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.message.Message;
import com.obj.nc.osk.dto.OskSendSmsResponseDto;
import com.obj.nc.osk.functions.senders.OskSmsSender;
import com.obj.nc.osk.functions.senders.OskSmsSenderConfigProperties;
import com.obj.nc.osk.service.OskSmsRestClientImpl;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.util.UriComponentsBuilder;

import static com.obj.nc.functions.processors.senders.BaseSmsSender.SEND_SMS_RESPONSE_ATTRIBUTE;
import static com.obj.nc.osk.service.OskSmsRestClientImpl.SEND_PATH;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ActiveProfiles(value = { "test"}, resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockRestServiceServer
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
class OskSmsSenderTest extends BaseIntegrationTest {

    @Autowired
    private OskSmsSender function;

    @Autowired
    private OskSmsSenderConfigProperties properties;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Test
    void testSendSms() {
        // GIVEN
        String MESSAGE_PATH = "smsNotificationMessages/message.json";
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_PATH, Message.class);

        // MOCK SERVER
        OskSendSmsResponseDto sendSmsResponseExpected = JsonUtils.readObjectFromClassPathResource("smsRestClient/sms-response-success.json", OskSendSmsResponseDto.class);

        mockRestServiceServer.expect(
                ExpectedCount.once(),
                requestTo(UriComponentsBuilder.fromHttpUrl(properties.getGapApiUrl() + SEND_PATH).build(properties.getSenderAddress()))
        )
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dGVzdGxvZ2luOnRlc3Rwdw=="))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(sendSmsResponseExpected))
                );

        // WHEN
        Message sentMessage = function.apply(inputMessage);

        // THEN
        mockRestServiceServer.verify();
        Assertions.assertThat(sentMessage.getBody().containsAttribute(SEND_SMS_RESPONSE_ATTRIBUTE)).isTrue();
        OskSendSmsResponseDto sendSmsResponseActual = sentMessage.getBody().getAttributeValueAs(SEND_SMS_RESPONSE_ATTRIBUTE, OskSendSmsResponseDto.class);
        Assertions.assertThat(sendSmsResponseActual).isEqualTo(sendSmsResponseExpected);
    }

}