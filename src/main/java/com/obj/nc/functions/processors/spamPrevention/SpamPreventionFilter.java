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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.function.Predicate;

import com.obj.nc.domain.deliveryOptions.SpamPreventionOption;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.extensions.providers.deliveryOptions.DeliveryOptionsProvider;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.DeliveryInfoRepository;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class SpamPreventionFilter implements Predicate<Message<?>> {
    private final DeliveryInfoRepository deliveryInfoRepository;
    private final DeliveryOptionsProvider deliveryOptionsProvider;

    @Override
    public boolean test(Message<?> payload) {
        for (ReceivingEndpoint endpoint : payload.getReceivingEndpoints()) {
            
            SpamPreventionOption spamPreventionOptions = deliveryOptionsProvider.findDeliveryOptions(endpoint).getSpamPrevention();

            if (spamPreventionOptions == null) {
                continue;
            }

            Instant startOfTimeInterval = TimeSpanCalculator.calculateNowMinusTimeFrame(
                OffsetDateTime.now(), 
                spamPreventionOptions.getMaxMessagesTimeFrame(), 
                spamPreventionOptions.getMaxMessagesUnit());

            long numberOfReceivedSince = deliveryInfoRepository.countByEndpointIdAndProcessedOnAfter(endpoint.getId(), startOfTimeInterval);

            if (numberOfReceivedSince >= spamPreventionOptions.getMaxMessages()) {
                log.debug("Messages are over limit. No more messages will be send to endpoint {}.", endpoint.getEndpointId());

                DeliveryInfo  discardedDI = DeliveryInfo.createDiscardedDeliveryInfo(payload.getId(), endpoint.getId());

                deliveryInfoRepository.save(discardedDI);
                return false;
            }
        }

        return true;
    }

}
