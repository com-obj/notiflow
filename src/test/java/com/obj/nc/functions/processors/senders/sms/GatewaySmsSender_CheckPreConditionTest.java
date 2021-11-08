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
import com.obj.nc.domain.endpoints.push.PushEndpoint;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class GatewaySmsSender_CheckPreConditionTest {
    @Test
    void testCheckPreCondition_NoEndpoints() {
        SmsMessage message = new SmsMessage();
        message.setBody(createBody());
        Optional<PayloadValidationException> exception = execCheckPreCondition(message);

        assertTrue(exception.isPresent());
        assertEquals("SmsSender at least one recipient (phone number) is required", exception.get().getMessage());
    }

    @Test
    void testCheckPreCondition_InvalidEndpoint() {
        SmsMessage message = new SmsMessage();
        message.setBody(createBody());
        message.setReceivingEndpoints(Collections.singletonList(PushEndpoint.ofToken("token")));
        Optional<PayloadValidationException> exception = execCheckPreCondition(message);

        assertTrue(exception.isPresent());
        assertEquals("SmsSender can send to SmsEndpoint endpoints only. Found DirectPushEndpoint(token=token)", exception.get().getMessage());
    }

    @Test
    void testCheckPreCondition() {
        SmsMessage message = new SmsMessage();
        message.setBody(createBody());
        message.setReceivingEndpoints(Collections.singletonList(createEndpoint()));
        Optional<PayloadValidationException> exception = execCheckPreCondition(message);

        assertFalse(exception.isPresent());
    }

    private SimpleTextContent createBody() {
        return SimpleTextContent.builder().text("text").build();
    }

    private Optional<PayloadValidationException> execCheckPreCondition(SmsMessage message) {
        return new GatewayApiSmsSenderImpl(null, null).checkPreCondition(message);
    }

    private SmsEndpoint createEndpoint() {
        return SmsEndpoint.builder().phone("+421950123456").build();
    }
}
