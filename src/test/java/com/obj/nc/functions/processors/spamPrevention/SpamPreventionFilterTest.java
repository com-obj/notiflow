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

import com.obj.nc.domain.deliveryOptions.EndpointDeliveryOptionsConfig;
import com.obj.nc.domain.deliveryOptions.SpamPreventionOption;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.extensions.providers.deliveryOptions.DeliveryOptionsProvider;
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
    DeliveryInfoRepository diRepo = Mockito.mock(DeliveryInfoRepository.class);
    DeliveryOptionsProvider doProvider = Mockito.mock(DeliveryOptionsProvider.class);

    SpamPreventionFilter filter = new SpamPreventionFilter(diRepo, doProvider);

    EmailEndpoint testEndpoint = EmailEndpoint.builder().email("test@test.com").build();

    @Test
    void testNoSpamPreventionOptions() {
        Mockito
            .when( doProvider.findDeliveryOptions( ArgumentMatchers.any()))
            .thenReturn(new EndpointDeliveryOptionsConfig());

        EmailMessage emailMessage = createEmailMessage();

        Assertions.assertTrue(filter.test(emailMessage));
    }

    @Test
    void testNotReachedLimit() {
        EmailMessage emailMessage = setSpamPreventionAndAlreadyDelivered(1, 0);

        Assertions.assertTrue(filter.test(emailMessage));

        verifyCountMethodCall(emailMessage);

        Mockito.verify(diRepo, Mockito.never()).save(ArgumentMatchers.any());
    }

    @Test
    void testAlreadyReachedLimit() {
        EmailMessage emailMessage = setSpamPreventionAndAlreadyDelivered(1, 1);

        Assertions.assertFalse(filter.test(emailMessage));

        verifyCountMethodCall(emailMessage);

        ArgumentCaptor<DeliveryInfo> captor = ArgumentCaptor.forClass(DeliveryInfo.class);
        Mockito.verify(diRepo).save(captor.capture());

        DeliveryInfo deliveryInfo = captor.getValue();
        Assertions.assertNotNull(deliveryInfo);
        Assertions.assertEquals(DeliveryInfo.DELIVERY_STATUS.DISCARDED, deliveryInfo.getStatus());
    }

    private EmailMessage setSpamPreventionAndAlreadyDelivered(int maxMessages, int deliveryInfoCount) {
        EndpointDeliveryOptionsConfig deliveryOptions = new EndpointDeliveryOptionsConfig();
        deliveryOptions.setSpamPrevention(new SpamPreventionOption(maxMessages, 1, SpamPreventionOption.MaxMessageUnit.MINUTES));
        Mockito
            .when( doProvider.findDeliveryOptions( ArgumentMatchers.any()))
            .thenReturn(deliveryOptions);

        EmailMessage emailMessage = createEmailMessage();
        mockCountMethodCall(deliveryInfoCount, emailMessage);
        return emailMessage;
    }


    private void mockCountMethodCall(long deliveryInfoCount, EmailMessage emailMessage) {
        Mockito.when(
            diRepo.countByEndpointIdAndProcessedOnAfter(
                ArgumentMatchers.eq(testEndpoint.getId()), 
                ArgumentMatchers.any())
            )
            .thenReturn(deliveryInfoCount);
    }

    private void verifyCountMethodCall(EmailMessage emailMessage) {
        Mockito
            .verify(diRepo)
            .countByEndpointIdAndProcessedOnAfter(
                ArgumentMatchers.eq(testEndpoint.getId()), 
                ArgumentMatchers.any()
            );
    }

    private EmailMessage createEmailMessage() {
        EmailMessage message = new EmailMessage();
        message.setReceivingEndpoints(Collections.singletonList(testEndpoint));
        return message;
    }
}
