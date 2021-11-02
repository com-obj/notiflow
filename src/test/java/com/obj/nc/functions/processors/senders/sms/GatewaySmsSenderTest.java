/*
 * Copyright (C) 2021 the original author or authors.
 * This file is part of Notiflow
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.obj.nc.functions.processors.senders.sms;

import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.SmsMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class GatewaySmsSenderTest {
    static final String PHONE = "+421950123456";
    static final String TEXT = "Greetings";
    GatewayApiConfig conf;

    GatewaySmsSenderTest() {
        conf = new GatewayApiConfig();
        conf.setSender("sender");
        conf.setSendSmsUrl("sendSmsUrl");
        conf.setToken("token");
    }

    @Test
    void testCall() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        Mockito.when(restTemplate.postForEntity(eq(conf.getSendSmsUrl()), any(), eq(String.class))).thenReturn(ResponseEntity.ok().build());

        SmsMessage message = new SmsMessage();
        message.setBody(SimpleTextContent.builder().text(TEXT).build());
        SmsEndpoint endpoint = SmsEndpoint.builder().phone(PHONE).build();
        message.setReceivingEndpoints(Collections.singletonList(endpoint));

        new GatewayApiSmsSenderImpl(restTemplate, conf).execute(message);

        verifyRequest(restTemplate);
    }

    private void verifyRequest(RestTemplate restTemplate) {
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        Mockito.verify(restTemplate).postForEntity(eq(conf.getSendSmsUrl()), captor.capture(), eq(String.class));

        HttpEntity value = captor.getValue();
        Assertions.assertNotNull(value);
        Assertions.assertEquals(MediaType.APPLICATION_FORM_URLENCODED, value.getHeaders().getContentType());

        MultiValueMap<String, String> map = (MultiValueMap<String, String>) value.getBody();
        Assertions.assertNotNull(map);
        Assertions.assertEquals(conf.getSender(), map.get("sender").get(0));
        Assertions.assertEquals(conf.getToken(), map.get("token").get(0));
        Assertions.assertEquals(TEXT, map.get("message").get(0));
        Assertions.assertEquals(PHONE, map.get("recipients.0.msisdn").get(0));
    }

    @Test
    void testResponseWithNot200HttpStatus() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        Mockito.when(restTemplate.postForEntity(eq(conf.getSendSmsUrl()), any(), eq(String.class))).thenReturn(ResponseEntity.badRequest().body("Problem occurred"));

        SmsMessage message = new SmsMessage();
        message.setBody(SimpleTextContent.builder().text(TEXT).build());
        SmsEndpoint endpoint = SmsEndpoint.builder().phone(PHONE).build();
        message.setReceivingEndpoints(Collections.singletonList(endpoint));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> new GatewayApiSmsSenderImpl(restTemplate, conf).execute(message));
        Assertions.assertEquals("SmsSender: GatewayApi responded with status code 400 and reason: Problem occurred.", thrown.getMessage());
    }
}
