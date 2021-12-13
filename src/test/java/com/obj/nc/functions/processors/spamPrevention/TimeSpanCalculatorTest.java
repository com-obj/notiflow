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

import com.obj.nc.domain.deliveryOptions.SpamPreventionOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;

class TimeSpanCalculatorTest {
    OffsetDateTime now = OffsetDateTime.now();

    @Test
    void testDaysSubtraction() {
        int limit = 1;
        Instant timePoint = TimeSpanCalculator.calculateNowMinusTimeFrame(now, limit, SpamPreventionOption.MaxMessageUnit.DAYS);
        Assertions.assertEquals(now.minusDays(limit).toInstant(), timePoint);
    }

    @Test
    void testHoursSubtraction() {
        int limit = 1;
        Instant timePoint = TimeSpanCalculator.calculateNowMinusTimeFrame(now, limit, SpamPreventionOption.MaxMessageUnit.HOURS);
        Assertions.assertEquals(now.minusHours(limit).toInstant(), timePoint);
    }

    @Test
    void testMinutesSubtraction() {
        int limit = 1;
        Instant timePoint = TimeSpanCalculator.calculateNowMinusTimeFrame(now, limit, SpamPreventionOption.MaxMessageUnit.MINUTES);
        Assertions.assertEquals(now.minusMinutes(limit).toInstant(), timePoint);
    }

    @Test
    void testNegativeLimit() {
        int limit = 0;
        Instant timePoint = TimeSpanCalculator.calculateNowMinusTimeFrame(now, limit, SpamPreventionOption.MaxMessageUnit.DAYS);
        Assertions.assertEquals(now.toInstant(), timePoint);
    }

}
