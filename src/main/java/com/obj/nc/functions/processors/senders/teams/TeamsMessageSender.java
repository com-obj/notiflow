/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.functions.processors.senders.teams;

import com.obj.nc.domain.content.teams.TeamsMessageContent;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.TeamsEndpoint;
import com.obj.nc.domain.message.TeamsMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static com.obj.nc.functions.processors.senders.config.GenericConfig.GENERIC_REST_TEMPLATE;

@Slf4j
@Component
public class TeamsMessageSender extends ProcessorFunctionAdapter<TeamsMessage, TeamsMessage> {
    private final RestTemplate restTemplate;

    public TeamsMessageSender(@Qualifier(GENERIC_REST_TEMPLATE) RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected Optional<PayloadValidationException> checkPreCondition(TeamsMessage payload) {
        Optional<PayloadValidationException> exception = super.checkPreCondition(payload);

        if (exception.isPresent()) {
            return exception;
        }

        TeamsMessageContent body = payload.getBody();

        if (body.getText().isEmpty()) {
            return Optional.of(new PayloadValidationException("TeamsMessageContent text cannot be empty"));
        }

        List<? extends ReceivingEndpoint> endpoints = payload.getReceivingEndpoints();

        if (endpoints.size() != 1) {
            return Optional.of(new PayloadValidationException("TeamsMessageSender can send to only one channel. Found more: " + endpoints));
        }

        ReceivingEndpoint endpoint = endpoints.get(0);

        if (!(endpoint instanceof TeamsEndpoint)) {
            return Optional.of(new PayloadValidationException("TeamsMessageSender can send to TeamsEndpoint endpoints only. Found " + endpoint));
        }

        return Optional.empty();
    }

    @Override
    protected TeamsMessage execute(TeamsMessage payload) {
        List<? extends ReceivingEndpoint> endpoints = payload.getReceivingEndpoints();
        String webhookUrl = endpoints.get(0).getEndpointId();

        ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, prepareRequest(payload.getBody()), String.class);
        verifyResponse(response);

        return payload;
    }

    private HttpEntity<String> prepareRequest(TeamsMessageContent content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>("{\"text\":\"" + content.getText() + "\"}", headers);
    }

    private void verifyResponse(ResponseEntity<String> response) {
        final int statusCode = response.getStatusCodeValue();

        if (statusCode != 200) {
            throw new RuntimeException("Error sending teams message. Service responded with code " + statusCode + " and Reason: " + response.getBody());
        }
    }
}
