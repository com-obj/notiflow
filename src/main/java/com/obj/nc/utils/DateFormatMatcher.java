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

package com.obj.nc.utils;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateFormatMatcher extends TypeSafeMatcher<String> {
    
    private static final SimpleDateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm'Z'");
    
    private SimpleDateFormat format;
    
    private DateFormatMatcher() {
    }
    
    private DateFormatMatcher(SimpleDateFormat format) {
        this.format = format;
    }
    
    public static DateFormatMatcher matchesFormat(SimpleDateFormat format) {
        return new DateFormatMatcher(format);
    }
    
    public static DateFormatMatcher matchesISO8601() {
        return matchesFormat(ISO8601_DATE_FORMAT);
    }
    
    @Override
    protected boolean matchesSafely(String item) {
        try {
            return format.parse(item) != null;
        } catch (ParseException e) {
            return false;
        }
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("matches date format= %s", format));
    }
    
}
