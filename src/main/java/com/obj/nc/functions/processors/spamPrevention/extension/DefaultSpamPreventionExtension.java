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

package com.obj.nc.functions.processors.spamPrevention.extension;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.deliveryOptions.DeliveryOptions;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Priority;

@Priority(value = Ordered.LOWEST_PRECEDENCE)
@Component
@RequiredArgsConstructor
public class DefaultSpamPreventionExtension implements SpamPreventionExtension {
    private final EndpointIdentifier endpointIdentifier;

    @Override
    public BasePayload<?> apply(BasePayload<?> payload) {
        for (ReceivingEndpoint endpoint : payload.getReceivingEndpoints()) {
            DeliveryOptions deliveryOptions = endpoint.getDeliveryOptions();

            if (deliveryOptions == null) {
                deliveryOptions = new DeliveryOptions();
            }

            SpamPreventionConfig config = endpointIdentifier.identify(endpoint);

            if (deliveryOptions.getSpamPrevention() != null) {
                config.option = deliveryOptions.getSpamPrevention();
            }

            OptionsValidator.validate(config);

            deliveryOptions.setSpamPrevention(config.option);
            endpoint.setDeliveryOptions(deliveryOptions);
        }

        return payload;
    }
}
