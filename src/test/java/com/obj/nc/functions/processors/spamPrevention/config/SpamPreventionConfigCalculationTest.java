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

package com.obj.nc.functions.processors.spamPrevention.config;

import com.obj.nc.domain.deliveryOptions.SpamPreventionOption;
import com.obj.nc.domain.deliveryOptions.SpamPreventionOption.MaxMessageUnit;
import com.obj.nc.domain.deliveryOptions.EndpointDeliveryOptionsConfig;
import com.obj.nc.domain.deliveryOptions.SpamPreventionGlobalConfigProperties;
import com.obj.nc.domain.endpoints.*;
import com.obj.nc.domain.endpoints.push.DirectPushEndpoint;
import com.obj.nc.functions.processors.spamPrevention.config.SpamPreventionConfigForChannel;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import javax.websocket.EndpointConfig;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(properties = {
    "nc.delivery.spam-prevention.email.maxMessages=1", 
    "nc.delivery.spam-prevention.email.maxMessagesTimeFrame=1",
    "nc.delivery.spam-prevention.email.maxMessagesUnit=DAYS",
    "nc.delivery.spam-prevention.slack.maxMessages=1", 
    "nc.delivery.spam-prevention.slack.maxMessagesTimeFrame=1",
    "nc.delivery.spam-prevention.slack.maxMessagesUnit=DAYS",
    "nc.delivery.spam-prevention.sms.maxMessages=1", 
    "nc.delivery.spam-prevention.sms.maxMessagesTimeFrame=1",
    "nc.delivery.spam-prevention.sms.maxMessagesUnit=DAYS",
    "nc.delivery.spam-prevention.teams.maxMessages=1", 
    "nc.delivery.spam-prevention.teams.maxMessagesTimeFrame=1",
    "nc.delivery.spam-prevention.teams.maxMessagesUnit=DAYS",
    "nc.delivery.spam-prevention.push.maxMessages=1", 
    "nc.delivery.spam-prevention.push.maxMessagesTimeFrame=1",
    "nc.delivery.spam-prevention.push.maxMessagesUnit=DAYS",})
public class SpamPreventionConfigCalculationTest {

    @ParameterizedTest
    @MethodSource("prepareTestInput")
    void testRightEndpointIdentification(TestInput input) {
        SpamPreventionConfigForChannel config = input.endpoint.calculateSpamPreventionOption();

        Assertions.assertEquals(config.channel, input.expactedChannel);
        Assertions.assertEquals(config.option, input.expectedOption);
    }

    static Stream<TestInput> prepareTestInput() {
        return Stream.of(
                createEmailInput(),
                createMailchimpInput(),
                createSlackInput(),
                createSmsInput(),
                createPushInput(),
                createTeamsInput());
    }

    static TestInput createMailchimpInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = MailchimpEndpoint.builder().email("test@test.com").build();
        testInput.expactedChannel = SpamPreventionConfigForChannel.Channel.EMAIL;
        testInput.expectedOption = createExpectedSpamPreventionOptionFromGlobal();
        return testInput;
    }

    static TestInput createEmailInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = EmailEndpoint.builder().email("test@test.com").build();
        testInput.expactedChannel = SpamPreventionConfigForChannel.Channel.EMAIL;

        SpamPreventionOption spamOptionForEndpoint = new SpamPreventionOption();
        spamOptionForEndpoint.setMaxMessages(5);
        spamOptionForEndpoint.setMaxMessagesTimeFrame(3);
        spamOptionForEndpoint.setMaxMessagesUnit(MaxMessageUnit.DAYS);

        EndpointDeliveryOptionsConfig endpointConfig= new EndpointDeliveryOptionsConfig();
        endpointConfig.setSpamPrevention(spamOptionForEndpoint);

        testInput.endpoint.setDeliveryOptions(endpointConfig);

        testInput.expectedOption = createExpectedSpamPreventionOptionFromEnpoint();
        return testInput;
    }

    static TestInput createSmsInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = SmsEndpoint.builder().phone("+421123456789").build();
        
        testInput.expactedChannel = SpamPreventionConfigForChannel.Channel.SMS;
        testInput.expectedOption = createExpectedSpamPreventionOptionFromGlobal();
        return testInput;
    }

    static TestInput createSlackInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = SlackEndpoint.builder().channel("public-channel").build();

        testInput.expactedChannel = SpamPreventionConfigForChannel.Channel.SLACK;
        testInput.expectedOption = createExpectedSpamPreventionOptionFromGlobal();
        return testInput;
    }

    static TestInput createPushInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = DirectPushEndpoint.ofTopic("push-topic");

        testInput.expactedChannel = SpamPreventionConfigForChannel.Channel.PUSH;
        testInput.expectedOption = createExpectedSpamPreventionOptionFromGlobal();
        return testInput;
    }

    static TestInput createTeamsInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = TeamsEndpoint.builder().webhookUrl("wild-webhook-url").build();

        testInput.expactedChannel = SpamPreventionConfigForChannel.Channel.TEAMS;
        testInput.expectedOption = createExpectedSpamPreventionOptionFromGlobal();
        return testInput;
    }

    static SpamPreventionOption createExpectedSpamPreventionOptionFromGlobal() {
        return new SpamPreventionOption(1, 1, SpamPreventionOption.MaxMessageUnit.DAYS);
    }

    static SpamPreventionOption createExpectedSpamPreventionOptionFromEnpoint() {
        return new SpamPreventionOption(5, 3, SpamPreventionOption.MaxMessageUnit.DAYS);
    }

    private static class TestInput {
        ReceivingEndpoint endpoint;

        SpamPreventionConfigForChannel.Channel expactedChannel;
        SpamPreventionOption expectedOption;
    }
}
