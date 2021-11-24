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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OptionsValidator {
    static void validate(SpamPreventionConfig config) {
        SpamPreventionOption props = config.option;
        if (props == null) {
            return;
        }

        if (props.getMaxMessages() == null) {
            throw new RuntimeException(createNotSetErrMessage("maxMessages", config.channel));
        }

        if (props.getMaxMessages() < 0) {
            throw new RuntimeException(createInvalidValueErrMessage("maxMessages", Integer.toString(props.getMaxMessages()), config.channel));
        }

        if (props.getMaxMessagesTimeFrame() == null) {
            throw new RuntimeException(createNotSetErrMessage("maxMessagesTimeFrame", config.channel));
        }

        if (props.getMaxMessagesTimeFrame() < 0) {
            throw new RuntimeException(createInvalidValueErrMessage("maxMessagesTimeFrame", Integer.toString(props.getMaxMessagesTimeFrame()), config.channel));
        }

        if (props.getMaxMessagesUnit() == null) {
            throw new RuntimeException(createInvalidMaxMessagesUnitValueErrMessage(config.channel));
        }
    }

    private static String createNotSetErrMessage(String propName, SpamPreventionConfig.Channel channel) {
        String template = "Property %s is not set, or has wrong value.";
        return String.format(template, propName) + " " + createGenericSentencePart(channel, propName);
    }

    private static String createInvalidValueErrMessage(String propName, String value, SpamPreventionConfig.Channel channel) {
        String template = "Property %s has invalid value %s.";
        return String.format(template, propName, value) + " " + createGenericSentencePart(channel, propName);
    }

    private static String createInvalidMaxMessagesUnitValueErrMessage(SpamPreventionConfig.Channel channel) {
        String propName = "maxMessagesUnit";
        String listOfValues = Arrays.stream(SpamPreventionOption.MaxMessageUnit.values())
                .map(SpamPreventionOption.MaxMessageUnit::name)
                .collect(Collectors.joining(","));
        String template = "Property %s has invalid value. Please set one of: %s.";
        return String.format(template, propName, listOfValues) + " " + createGenericSentencePart(channel, propName);
    }

    private static String createGenericSentencePart(SpamPreventionConfig.Channel channel, String propName) {
        String template = "Please set nc.delivery.spam-prevention.%s.%s in application.properties.";
        return String.format(template, channel.name().toLowerCase(Locale.ROOT), propName);
    }
}
