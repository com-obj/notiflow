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

package com.obj.nc.functions.processors.delivery;

import com.obj.nc.domain.delivery.Message2EndpointRelation;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.endpointPersister.EndpointPersister;
import com.obj.nc.functions.processors.messagePersister.MessagePersister;
import com.obj.nc.repositories.Message2EndpointRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class MessageAndEndpointPersister extends ProcessorFunctionAdapter<Message<?>, Message<?>> {
    private final MessagePersister messagePersister;
    private final EndpointPersister endpointPersister;
    private final Message2EndpointRelationRepository deliveryRepo;

    @Override
    protected Message<?> execute(Message<?> payload) {
        Message<?> persisted = (Message<?>) endpointPersister.apply(payload);
        persisted = messagePersister.apply(persisted);

        for (ReceivingEndpoint endpoint : persisted.getReceivingEndpoints()) {
            if (deliveryRepo.existsByEndpointIdAndMessageId(endpoint.getId(), persisted.getId())) {
                continue;
            }

            Message2EndpointRelation options = new Message2EndpointRelation();
            options.setMessageId(persisted.getId());
            options.setEndpointId(endpoint.getId());
            options.setDeliveryOptions(endpoint.getDeliveryOptions());
            deliveryRepo.save(options);
        }

        return persisted;
    }
}
