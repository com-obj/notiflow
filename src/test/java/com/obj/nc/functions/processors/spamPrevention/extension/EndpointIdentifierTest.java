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

import com.obj.nc.domain.deliveryOptions.SpamPreventionOption;
import com.obj.nc.domain.deliveryOptions.SpamPreventionProperties;
import com.obj.nc.domain.endpoints.*;
import com.obj.nc.domain.endpoints.push.DirectPushEndpoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class EndpointIdentifierTest {
    @ParameterizedTest
    @MethodSource("prepareTestInput")
    void testRightEndpointIdentification(TestInput input) {
        EndpointIdentifier endpointIdentifier = new EndpointIdentifier(input.globalConfig);
        SpamPreventionConfig config = endpointIdentifier.identify(input.endpoint);

        Assertions.assertEquals(config.channel, input.channel);
        Assertions.assertEquals(config.option, input.config);
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
        testInput.channel = SpamPreventionConfig.Channel.EMAIL;
        testInput.config = createSpamPreventionOption();
        testInput.globalConfig = new SpamPreventionProperties(testInput.config, null, null, null, null);
        return testInput;
    }

    static TestInput createEmailInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = EmailEndpoint.builder().email("test@test.com").build();
        testInput.channel = SpamPreventionConfig.Channel.EMAIL;
        testInput.config = createSpamPreventionOption();
        testInput.globalConfig = new SpamPreventionProperties(testInput.config, null, null, null, null);
        return testInput;
    }

    static TestInput createSmsInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = SmsEndpoint.builder().phone("+421123456789").build();
        testInput.channel = SpamPreventionConfig.Channel.SMS;
        testInput.config = createSpamPreventionOption();
        testInput.globalConfig = new SpamPreventionProperties(null, null, testInput.config, null, null);
        return testInput;
    }

    static TestInput createSlackInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = SlackEndpoint.builder().channel("public-channel").build();
        testInput.channel = SpamPreventionConfig.Channel.SLACK;
        testInput.config = createSpamPreventionOption();
        testInput.globalConfig = new SpamPreventionProperties(null, testInput.config, null, null, null);
        return testInput;
    }

    static TestInput createPushInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = DirectPushEndpoint.ofTopic("push-topic");
        testInput.channel = SpamPreventionConfig.Channel.PUSH;
        testInput.config = createSpamPreventionOption();
        testInput.globalConfig = new SpamPreventionProperties(null, null, null, null, testInput.config);
        return testInput;
    }

    static TestInput createTeamsInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = TeamsEndpoint.builder().webhookUrl("wild-webhook-url").build();
        testInput.channel = SpamPreventionConfig.Channel.TEAMS;
        testInput.config = createSpamPreventionOption();
        testInput.globalConfig = new SpamPreventionProperties(null, null, null, testInput.config, null);
        return testInput;
    }

    static SpamPreventionOption createSpamPreventionOption() {
        return new SpamPreventionOption(1, 1, SpamPreventionOption.MaxMessageUnit.DAYS);
    }

    private static class TestInput {
        ReceivingEndpoint endpoint;
        SpamPreventionConfig.Channel channel;
        SpamPreventionOption config;
        SpamPreventionProperties globalConfig;

    }
}
