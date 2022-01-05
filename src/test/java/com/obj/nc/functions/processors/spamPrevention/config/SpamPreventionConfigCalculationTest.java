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

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

import java.util.stream.Stream;

import com.obj.nc.domain.deliveryOptions.EndpointDeliveryOptionsConfig;
import com.obj.nc.domain.deliveryOptions.SpamPreventionOption;
import com.obj.nc.domain.deliveryOptions.SpamPreventionOption.MaxMessageUnit;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SlackEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.endpoints.TeamsEndpoint;
import com.obj.nc.domain.endpoints.push.DirectPushEndpoint;
import com.obj.nc.extensions.providers.deliveryOptions.JsonRepoDeliveryOptionsProvider;
import com.obj.nc.extensions.providers.deliveryOptions.SpamPreventionConfigProperties;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(properties = {
    "nc.delivery.spam-prevention.jsonRepoPathAndFileName=src/test/resources/deliver-options/delivery-options-setting.json", 
})
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
public class SpamPreventionConfigCalculationTest extends BaseIntegrationTest {

    @Autowired
    protected SpamPreventionConfigProperties config;
    @Autowired
    protected JsonRepoDeliveryOptionsProvider doProvider;

    @ParameterizedTest
    @MethodSource("prepareTestInput")
    void testRightEndpointIdentification(TestInput input) {
        EndpointDeliveryOptionsConfig delOpt = doProvider.findDeliveryOptions(input.endpoint);

        Assertions.assertEquals(delOpt.getSpamPrevention(), input.expectedOption);
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
        testInput.endpoint = MailchimpEndpoint.builder().email("global").build();

        testInput.expectedOption = createExpectedSpamPreventionOptionGlobal();
        return testInput;
    }

    static TestInput createEmailInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = EmailEndpoint.builder().email("test@test.com").build();

        testInput.expectedOption = createExpectedSpamPreventionOptionForEnpoint();
        return testInput;
    }

    static TestInput createSmsInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = SmsEndpoint.builder().phone("global").build();
        
        testInput.expectedOption = createExpectedSpamPreventionOptionGlobal();
        return testInput;
    }

    static TestInput createSlackInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = SlackEndpoint.builder().channel("public-channel").build();

        testInput.expectedOption = createExpectedSpamPreventionOptionForEnpoint();
        return testInput;
    }

    static TestInput createPushInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = DirectPushEndpoint.ofTopic("push-topic");

        testInput.expectedOption = createExpectedSpamPreventionOptionForEnpoint();
        return testInput;
    }

    static TestInput createTeamsInput() {
        TestInput testInput = new TestInput();
        testInput.endpoint = TeamsEndpoint.builder().webhookUrl("wild-webhook-url").build();

        testInput.expectedOption = createExpectedSpamPreventionOptionForEnpoint();
        return testInput;
    }

    static SpamPreventionOption createExpectedSpamPreventionOptionGlobal() {
        return new SpamPreventionOption(1, 1, SpamPreventionOption.MaxMessageUnit.DAYS);
    }

    static SpamPreventionOption createExpectedSpamPreventionOptionForEnpoint() {
        return new SpamPreventionOption(5, 3, SpamPreventionOption.MaxMessageUnit.DAYS);
    }

    private static class TestInput {
        ReceivingEndpoint endpoint;

        SpamPreventionOption expectedOption;
    }
}
