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

package com.obj.nc.extensions.providers.deliveryOptions;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.obj.nc.domain.deliveryOptions.SpamPreventionOption;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpamPreventionOptionsValidator {

    public static void validate(SpamPreventionOption config) {
        if (config == null) {
            return;
        }

        if (config.getMaxMessages() == null) {
            throw new RuntimeException(createNotSetErrMessage("maxMessages"));
        }

        if (config.getMaxMessages() < 0) {
            throw new RuntimeException(createInvalidValueErrMessage("maxMessages", Integer.toString(config.getMaxMessages())));
        }

        if (config.getMaxMessagesTimeFrame() == null) {
            throw new RuntimeException(createNotSetErrMessage("maxMessagesTimeFrame" ));
        }

        if (config.getMaxMessagesTimeFrame() < 0) {
            throw new RuntimeException(createInvalidValueErrMessage("maxMessagesTimeFrame", Integer.toString(config.getMaxMessagesTimeFrame())));
        }

        if (config.getMaxMessagesUnit() == null) {
            throw new RuntimeException(createInvalidMaxMessagesUnitValueErrMessage());
        }
    }

    private static String createNotSetErrMessage(String propName) {
        String template = "Property %s is not set, or has wrong value.";
        return String.format(template, propName);
    }

    private static String createInvalidValueErrMessage(String propName, String value) {
        String template = "Property %s has invalid value %s.";
        return String.format(template, propName, value);
    }

    private static String createInvalidMaxMessagesUnitValueErrMessage() {
        String propName = "maxMessagesUnit";
        String listOfValues = Arrays.stream(SpamPreventionOption.MaxMessageUnit.values())
                .map(SpamPreventionOption.MaxMessageUnit::name)
                .collect(Collectors.joining(","));
        String template = "Property %s has invalid value. Please set one of: %s.";
        return String.format(template, propName, listOfValues);
    }

    
}
