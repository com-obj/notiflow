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

package com.obj.nc.functions.processors.spamPrevention;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.deliveryOptions.DeliveryOptions;
import com.obj.nc.domain.deliveryOptions.SpamPreventionOption;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.DeliveryInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
@Component
public class SpamPreventionFilter implements Predicate<BasePayload<?>> {
    private final DeliveryInfoRepository deliveryInfoRepository;

    @Override
    public boolean test(BasePayload<?> payload) {
        for (ReceivingEndpoint endpoint : payload.getReceivingEndpoints()) {
            DeliveryOptions deliveryOptions = endpoint.getDeliveryOptions();

            if (deliveryOptions == null || deliveryOptions.getSpamPrevention() == null) {
                continue;
            }
            SpamPreventionOption spamPreventionOptions = deliveryOptions.getSpamPrevention();

            Instant instant = TimeSpanCalculator.calculate(OffsetDateTime.now(), spamPreventionOptions);

            long count = deliveryInfoRepository.countByEndpointIdAndProcessedOnAfter(endpoint.getId(), instant);

            if (count >= spamPreventionOptions.getMaxMessages()) {
                log.debug("Messages are over limit. No more messages will be send to endpoint {}.", endpoint.getEndpointId());
                DeliveryInfo deliveryInfo = DeliveryInfo.builder().endpointId(endpoint.getId()).messageId(payload.getId()).status(DeliveryInfo.DELIVERY_STATUS.DISCARDED).build();
                deliveryInfoRepository.save(deliveryInfo);
                return false;
            }
        }

        return true;
    }
}
