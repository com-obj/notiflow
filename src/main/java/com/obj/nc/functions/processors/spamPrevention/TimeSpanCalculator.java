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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class TimeSpanCalculator {

    static Instant calculateNowMinusTimeFrame(OffsetDateTime from, Integer timeFrame, SpamPreventionOption.MaxMessageUnit unit) {
        if (timeFrame == 0) {
            return from.toInstant();
        }

        OffsetDateTime date;

        switch (unit) {
            case DAYS:
                date = from.minusDays(timeFrame);
                break;
            case HOURS:
                date = from.minusHours(timeFrame);
                break;
            case MINUTES:
                date = from.minusMinutes(timeFrame);
                break;
            default:
                throw new IllegalStateException("Unsupported timeFrame unit: " + unit);
        }

        return date.toInstant();
    }
}
