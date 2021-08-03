package com.obj.nc.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

public final class QueryUtils {
    
    private QueryUtils() {
    }
    
    public static String toLikeFilter(Object filter) {
        if (filter == null || StringUtils.isBlank(filter.toString())) {
            return "%";
        }
        
        return "%" + filter.toString() + "%";
    }
    
    public static String toPaginationString(Pageable pageable) {
        return toPaginationString(pageable, new HashMap<>());
    }
    
    public static String toPaginationString(Pageable pageable, Map<String, String> propertyOverrides) {
        StringBuilder result = new StringBuilder();
        
        if (pageable.getSort().isSorted()) {
            result.append("order by ");
            result.append(pageable.getSort().get().map(order -> camelCaseToUnderscoreString(resolvePropertyName(order.getProperty(), propertyOverrides)) + " " + order.getDirection()).collect(Collectors.joining(", ")));
            result.append(" ");
        }
        
        result.append("offset ").append(pageable.getOffset()).append(" rows ");
        result.append("fetch next ").append(pageable.getPageSize()).append(" rows only");
        
        return result.toString();
    }
    
    private static String resolvePropertyName(String property, Map<String, String> propertyOverrides) {
        return propertyOverrides.getOrDefault(property, property);
    }
    
    private static String camelCaseToUnderscoreString(String camelCaseString) {
        if (StringUtils.isBlank(camelCaseString)) {
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        char[] chars = camelCaseString.toCharArray();
        
        for (int i = 0; i < chars.length - 1; i++) {
            if (Character.isLowerCase(chars[i]) && (Character.isUpperCase(chars[i + 1]) || Character.isDigit(chars[i + 1]))) {
                builder.append(chars[i]).append("_");
            } else if (Character.isUpperCase(chars[i]) && Character.isDigit(chars[i + 1])) {
                builder.append(Character.toLowerCase(chars[i])).append(chars[i]).append("_");
            } else if (Character.isDigit(chars[i]) && (Character.isLetter(chars[i + 1]))) {
                builder.append(chars[i]).append("_");
            } else {
                builder.append(Character.toLowerCase(chars[i]));
            }
        }
        builder.append(Character.toLowerCase(chars[chars.length - 1]));
        
        return builder.toString();
    }
    
}
