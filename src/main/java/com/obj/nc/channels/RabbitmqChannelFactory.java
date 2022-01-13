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

package com.obj.nc.channels;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.domain.BasePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class RabbitmqChannelFactory implements ChannelFactory {
    private final ConnectionFactory connectionFactory;
    private final ObjectMapper objectMapper;

    @Override
    public MessageChannel getPublishSubscribeChannel(String channelName) {
        return Amqp.publishSubscribeChannel(connectionFactory).queueName(channelName).amqpMessageConverter(new org.springframework.amqp.support.converter.MessageConverter() {
            @Override
            public org.springframework.amqp.core.Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
                GenericMessage<?> genericMessage = (GenericMessage<?>) object;
                try {
                    byte[] body = objectMapper.writeValueAsBytes(genericMessage.getPayload());

                    if (log.isDebugEnabled()) {
                        log.debug("Sending json: {}.", new String(body));
                    }

                    return new org.springframework.amqp.core.Message(body, messageProperties);
                } catch (JsonProcessingException e) {
                    throw new MessageConversionException("Failed to serialize json", e);
                }
            }

            @Override
            public Object fromMessage(org.springframework.amqp.core.Message message) throws MessageConversionException {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Received json: {}.", new String(message.getBody()));
                    }

                    BasePayload basePayload = objectMapper.readValue(message.getBody(), BasePayload.class);
                    return basePayload;
                } catch (IOException e) {
                    throw new MessageConversionException("Failed to deserialize json", e);
                }
            }
        }).get();
    }
}
