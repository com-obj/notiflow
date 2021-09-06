package com.obj.nc.utils;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;

import com.google.common.base.CaseFormat;

public final class QueryUtils {
    
    private QueryUtils() {
    }
    
    public static String toPaginationString(Pageable pageable) {
        StringBuilder result = new StringBuilder();
        
        if (pageable.getSort().isSorted()) {
            result.append("order by ");
            result.append(pageable.getSort().get().map(order -> camelCaseToUnderscoreString(order.getProperty()) + " " + order.getDirection()).collect(Collectors.joining(", ")));
            result.append(" ");
        }
        
        result.append("offset ").append(pageable.getOffset()).append(" rows ");
        result.append("fetch next ").append(pageable.getPageSize()).append(" rows only");
        
        return result.toString();
    }
    
    private static String camelCaseToUnderscoreString(String camelCaseString) {
        if (StringUtils.isBlank(camelCaseString)) {
            return "";
        }
        
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camelCaseString);
    }
    
}
