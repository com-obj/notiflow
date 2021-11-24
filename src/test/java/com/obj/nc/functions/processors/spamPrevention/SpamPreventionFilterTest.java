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

import com.obj.nc.domain.deliveryOptions.DeliveryOptions;
import com.obj.nc.domain.deliveryOptions.SpamPreventionOption;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.DeliveryInfoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.UUID;

public class SpamPreventionFilterTest {
    DeliveryInfoRepository repo = Mockito.mock(DeliveryInfoRepository.class);
    SpamPreventionFilter filter = new SpamPreventionFilter(repo);

    @Test
    void testNoDeliveryOptions() {
        Assertions.assertTrue(filter.test(createEmailMessage(null)));
    }

    @Test
    void testNoSpamPreventionOptions() {
        Assertions.assertTrue(filter.test(createEmailMessage(new DeliveryOptions())));
    }

    @Test
    void testNotReachedLimit() {
        EmailMessage emailMessage = prepareDataAndMocks(0L);

        Assertions.assertTrue(filter.test(emailMessage));

        verifyCountMethodCall(emailMessage);
        Mockito.verify(repo, Mockito.never()).save(ArgumentMatchers.any());
    }

    @Test
    void testAlreadyReachedLimit() {
        EmailMessage emailMessage = prepareDataAndMocks(1L);

        Assertions.assertFalse(filter.test(emailMessage));

        verifyCountMethodCall(emailMessage);

        ArgumentCaptor<DeliveryInfo> captor = ArgumentCaptor.forClass(DeliveryInfo.class);
        Mockito.verify(repo).save(captor.capture());

        DeliveryInfo deliveryInfo = captor.getValue();
        Assertions.assertEquals(DeliveryInfo.DELIVERY_STATUS.DISCARDED, deliveryInfo.getStatus());
    }

    private EmailMessage prepareDataAndMocks(long deliveryInfoCount) {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setSpamPrevention(new SpamPreventionOption(1, 1, SpamPreventionOption.MaxMessageUnit.MINUTES));

        EmailMessage emailMessage = createEmailMessage(deliveryOptions);
        mockCountMethodCall(deliveryInfoCount, emailMessage);
        return emailMessage;
    }

    private void mockCountMethodCall(long deliveryInfoCount, EmailMessage emailMessage) {
        Mockito.when(repo.countByEndpointIdAndProcessedOnAfter(ArgumentMatchers.eq(getEndpointId(emailMessage)), ArgumentMatchers.any())).thenReturn(deliveryInfoCount);
    }

    private void verifyCountMethodCall(EmailMessage emailMessage) {
        Mockito.verify(repo).countByEndpointIdAndProcessedOnAfter(ArgumentMatchers.eq(getEndpointId(emailMessage)), ArgumentMatchers.any());
    }

    private UUID getEndpointId(EmailMessage emailMessage) {
        return emailMessage.getReceivingEndpoints().get(0).getId();
    }

    private EmailMessage createEmailMessage(DeliveryOptions deliveryOptions) {
        EmailEndpoint endpoint = EmailEndpoint.builder().email("test@test.com").build();
        endpoint.setDeliveryOptions(deliveryOptions);

        EmailMessage message = new EmailMessage();
        message.setReceivingEndpoints(Collections.singletonList(endpoint));
        return message;
    }
}
