/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
