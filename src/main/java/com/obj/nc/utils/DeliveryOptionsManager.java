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

package com.obj.nc.utils;

import com.obj.nc.domain.delivery.DeliveryOptionsEntity;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.repositories.DeliveryOptionsRepository;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class DeliveryOptionsManager {
    private final DeliveryOptionsRepository deliveryOptionsRepository;
    private final MessageRepository messageRepository;
    private final EndpointsRepository endpointsRepository;

    public Optional<Message<?>> reconstructMessage(UUID messageId) {
        List<DeliveryOptionsEntity> deliveryOptions = deliveryOptionsRepository.findByMessageId(messageId);

        if (deliveryOptions.isEmpty()) {
            return Optional.empty();
        }

        MessagePersistentState messagePersistentState = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Internal error: Message with ID " +messageId+ "was expected here."));

        Iterable<ReceivingEndpoint> endpoints = endpointsRepository.findByIds(deliveryOptions.stream().map(DeliveryOptionsEntity::getEndpointId).collect(Collectors.toList()).toArray(new UUID[deliveryOptions.size()]));

        List<ReceivingEndpoint> endpointList = new ArrayList<>();
        endpoints.forEach(endpoint -> {
            deliveryOptions.stream().filter(options -> endpoint.getId().equals(options.getEndpointId())).findFirst().ifPresent(options -> endpoint.setDeliveryOptions(options.getDeliveryOptions()));
            endpointList.add(endpoint);
        });

        Message<?> message = messagePersistentState.toMessage();
        message.setReceivingEndpoints(endpointList);
        return Optional.of(message);
    }
}
