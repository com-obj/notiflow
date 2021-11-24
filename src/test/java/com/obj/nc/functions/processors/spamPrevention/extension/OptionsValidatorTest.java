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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OptionsValidatorTest {
    SpamPreventionConfig config;

    OptionsValidatorTest() {
        config = new SpamPreventionConfig();
        config.channel = SpamPreventionConfig.Channel.EMAIL;
    }

    @Test
    void testNoMaxMessagesOptionSet() {
        config.option = new SpamPreventionOption();

        String expected = "Property maxMessages is not set, or has wrong value. Please set nc.delivery.spam-prevention.email.maxMessages in application.properties.";
        runAndVerifyCaughtMessage(config, expected);
    }

    @Test
    void testInvalidMaxMessagesOptionSet() {
        config.option = new SpamPreventionOption(-1, null, null);

        String expected = "Property maxMessages has invalid value -1. Please set nc.delivery.spam-prevention.email.maxMessages in application.properties.";
        runAndVerifyCaughtMessage(config, expected);
    }

    @Test
    void testNoMaxMessagesTimeFrameOptionSet() {
        config.option = new SpamPreventionOption(1, null, null);

        String expected = "Property maxMessagesTimeFrame is not set, or has wrong value. Please set nc.delivery.spam-prevention.email.maxMessagesTimeFrame in application.properties.";
        runAndVerifyCaughtMessage(config, expected);
    }

    @Test
    void testInvalidMaxMessagesTimeFrameOptionSet() {
        config.option = new SpamPreventionOption(1, -1, null);

        String expected = "Property maxMessagesTimeFrame has invalid value -1. Please set nc.delivery.spam-prevention.email.maxMessagesTimeFrame in application.properties.";
        runAndVerifyCaughtMessage(config, expected);
    }

    @Test
    void testNoMaxMessagesUnitOptionSet() {
        config.option = new SpamPreventionOption(1, 1, null);

        String expected = "Property maxMessagesUnit has invalid value. Please set one of: MINUTES,HOURS,DAYS. Please set nc.delivery.spam-prevention.email.maxMessagesUnit in application.properties.";
        runAndVerifyCaughtMessage(config, expected);
    }

    private void runAndVerifyCaughtMessage(SpamPreventionConfig config, String expectedMessage) {
        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> OptionsValidator.validate(config));
        Assertions.assertEquals(expectedMessage, thrown.getMessage());
    }
}
