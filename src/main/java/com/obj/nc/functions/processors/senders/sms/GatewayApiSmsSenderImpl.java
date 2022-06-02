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
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.SmsSender;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static com.obj.nc.config.PureRestTemplateConfig.PURE_REST_TEMPLATE;

@Component
//@Primary // TODO: not working with TestMode
@ConditionalOnProperty(prefix = "nc.sms", name = "gateway-api")	
public class GatewayApiSmsSenderImpl extends ProcessorFunctionAdapter<SmsMessage, SmsMessage> implements SmsSender {
    private final RestTemplate restTemplate;
    private final GatewayApiConfig config;

    public GatewayApiSmsSenderImpl(@Qualifier(PURE_REST_TEMPLATE) RestTemplate restTemplate, GatewayApiConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    @Override
    protected Optional<PayloadValidationException> checkPreCondition(SmsMessage payload) {
        Optional<PayloadValidationException> exception = super.checkPreCondition(payload);

        if (exception.isPresent()) {
            return exception;
        }

        List<? extends ReceivingEndpoint> endpoints = payload.getReceivingEndpoints();

        if (endpoints.isEmpty()) {
            return Optional.of(new PayloadValidationException("SmsSender at least one recipient (phone number) is required"));
        }

        return endpoints.stream()
                .filter(endpoint -> !(endpoint instanceof SmsEndpoint))
                .findAny()
                .map(receivingEndpoint -> new PayloadValidationException("SmsSender can send to SmsEndpoint endpoints only. Found " + receivingEndpoint));
    }

    @Override
    protected SmsMessage execute(SmsMessage payload) {
        ResponseEntity<String> response = restTemplate.postForEntity(config.getSendSmsUrl(), prepareRequest(payload), String.class);

        int statusCode = response.getStatusCodeValue();

        if (statusCode != 200) {
            String msg = String.format("SmsSender: GatewayApi responded with status code %s and reason: %s.", statusCode, response.getBody());
            throw new RuntimeException(msg);
        }

        return payload;
    }

    private HttpEntity<MultiValueMap<String, String>> prepareRequest(SmsMessage payload) {
        SimpleTextContent content = payload.getBody();
        List<? extends ReceivingEndpoint> endpoints = payload.getReceivingEndpoints();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("sender", config.getSender());
        map.add("message", content.getText());
        map.add("token", config.getToken());
        for (int i = 0; i < endpoints.size(); ++i) {
            map.add("recipients." + i + ".msisdn", endpoints.get(i).getEndpointId());
        }
        return new HttpEntity<>(map, headers);
    }
}
