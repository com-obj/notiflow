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
