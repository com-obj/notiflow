/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.functions.processors.dummy;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
//TODO: Does this test has any meaning??
class RecipientsUsingSubscriptionTest extends BaseIntegrationTest {

    private final DummyRecepientsEnrichmentProcessingFunction resolveRecipients =
            new DummyRecepientsEnrichmentProcessingFunction();

    @Test
    void testResolveRecipientsPassWithRequiredAttributes() {
        // given
		NotificationIntent notificationIntent = NotificationIntent.createWithStaticContent(
			"Business Intelligence (BI) Developer", 
			"We are looking for a Business Intelligence"
		);  

        // when
        NotificationIntent outputNotificationIntent = (NotificationIntent)resolveRecipients.apply(notificationIntent);

        // then
        MatcherAssert.assertThat(outputNotificationIntent, Matchers.notNullValue());
    }

    @Test
    void testResolveRecipientsResolvesAll() {
        // given
		NotificationIntent notificationIntent = NotificationIntent.createWithStaticContent(
			"Business Intelligence (BI) Developer", 
			"We are looking for a Business Intelligence"
		);  

        // when
        NotificationIntent outputNotificationIntent = (NotificationIntent)resolveRecipients.apply(notificationIntent);

        // then
        MatcherAssert.assertThat(outputNotificationIntent, Matchers.notNullValue());

        List<? extends ReceivingEndpoint> outputEventEndpoints = outputNotificationIntent.getReceivingEndpoints();
        MatcherAssert.assertThat(outputEventEndpoints, Matchers.hasSize(3));
        MatcherAssert.assertThat(outputEventEndpoints, Matchers.everyItem(Matchers.instanceOf(EmailEndpoint.class)));

        MatcherAssert.assertThat(outputEventEndpoints.get(0).getRecipient().getName(), Matchers.equalTo("John Doe"));
        MatcherAssert.assertThat(((EmailEndpoint) outputEventEndpoints.get(0)).getEmail(), Matchers.equalTo("john.doe@objectify.sk"));

        MatcherAssert.assertThat(outputEventEndpoints.get(1).getRecipient().getName(), Matchers.equalTo("John Dudly"));
        MatcherAssert.assertThat(((EmailEndpoint) outputEventEndpoints.get(1)).getEmail(), Matchers.equalTo("john.dudly@objectify.sk"));

        MatcherAssert.assertThat(outputEventEndpoints.get(2).getRecipient().getName(), Matchers.equalTo("All Objectify"));
        MatcherAssert.assertThat(((EmailEndpoint) outputEventEndpoints.get(2)).getEmail(), Matchers.equalTo("all@objectify.sk"));
    }

    @Test
    void testResolveRecipientsMergeWithExisting() {
        // given
        NotificationIntent inputNotificationIntent = NotificationIntent.createWithStaticContent(
            "Business Intelligence (BI) Developer", 
            "We are looking for a Business Intelligence", 
            EmailEndpoint.builder().email("john.doe@objectify.sk").build(),
            EmailEndpoint.builder().email("john.dudly@objectify.sk").build(),
            EmailEndpoint.builder().email("all@objectify.sk").build()
        );   

        // when
        NotificationIntent outputNotificationIntent = (NotificationIntent)resolveRecipients.apply(inputNotificationIntent);

        // then
        List<? extends ReceivingEndpoint> outputEventEndpoints = outputNotificationIntent.getReceivingEndpoints();
        MatcherAssert.assertThat(outputEventEndpoints, Matchers.hasSize(6));
        MatcherAssert.assertThat(outputEventEndpoints, Matchers.everyItem(Matchers.instanceOf(EmailEndpoint.class)));
    }

}