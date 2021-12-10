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
import com.obj.nc.domain.deliveryOptions.SpamPreventionOption;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

public class DefaultSpamPreventionExtensionTest {
    SpamPreventionConfigForChannel config = new SpamPreventionConfigForChannel();
    EndpointIdentifier identifier = Mockito.mock(EndpointIdentifier.class);

    @Test
    void testNoDeliveryOptionsAndNoSpamPreventionOptionSet() {
        DeliveryOptions deliveryOptions = runExtensionAndGetVerifiedDeliveryOptions(null);
        Assertions.assertNull(deliveryOptions.getSpamPrevention());
    }

    @Test
    void testNoDeliveryOptionsAndGlobalSpamPreventionOptionApplied() {
        config.option = createSpamPreventionOption();

        DeliveryOptions deliveryOptions = runExtensionAndGetVerifiedDeliveryOptions(null);
        Assertions.assertEquals(config.option, deliveryOptions.getSpamPrevention());
    }

    @Test
    void testSpecificSpamPreventionOptionApplied() {
        config.option = createSpamPreventionOption();

        DeliveryOptions deliveryOptions = new DeliveryOptions();
        SpamPreventionOption specificSpamPrevention = createSpamPreventionOption();
        deliveryOptions.setSpamPrevention(specificSpamPrevention);

        DeliveryOptions outputDeliveryOptions = runExtensionAndGetVerifiedDeliveryOptions(deliveryOptions);

        Assertions.assertNotNull(outputDeliveryOptions.getSpamPrevention());
        Assertions.assertEquals(specificSpamPrevention, outputDeliveryOptions.getSpamPrevention());
    }

    private DeliveryOptions runExtensionAndGetVerifiedDeliveryOptions(DeliveryOptions inputDeliveryOptions) {
        Mockito.when(identifier.identify(ArgumentMatchers.any())).thenReturn(config);

        DefaultSpamPreventionExtension extension = new DefaultSpamPreventionExtension(identifier);
        BasePayload<?> updated = extension.apply(createEmailMessage(inputDeliveryOptions));

        Assertions.assertNotNull(updated);

        List<? extends ReceivingEndpoint> receivingEndpoints = updated.getReceivingEndpoints();
        Assertions.assertNotNull(receivingEndpoints);
        Assertions.assertEquals(1, receivingEndpoints.size());

        DeliveryOptions deliveryOptions = receivingEndpoints.get(0).getDeliveryOptions();
        Assertions.assertNotNull(deliveryOptions);
        return deliveryOptions;
    }

    private EmailMessage createEmailMessage(DeliveryOptions deliveryOptions) {
        EmailEndpoint endpoint = EmailEndpoint.builder().email("test@test.com").build();
        endpoint.setDeliveryOptions(deliveryOptions);

        EmailMessage message = new EmailMessage();
        message.setReceivingEndpoints(Collections.singletonList(endpoint));
        return message;
    }

    private SpamPreventionOption createSpamPreventionOption() {
        return new SpamPreventionOption(1, 1, SpamPreventionOption.MaxMessageUnit.DAYS);
    }
}
