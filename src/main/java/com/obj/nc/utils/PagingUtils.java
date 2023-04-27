package com.obj.nc.utils;

import com.obj.nc.config.NcAppConfigProperties.Web.Paging;
import com.obj.nc.exceptions.PayloadValidationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.function.Predicate;

import static java.lang.String.format;

public class PagingUtils {
    public static Pageable createPageRequest(int pageNumber, int pageSize, Paging pagingProps) {
        int maxPageSize = pagingProps.getMaxPageSize();
        boolean oneIndexedParameters = pagingProps.isOneIndexedParameters();
        int lowerBound = oneIndexedParameters ? 1 : 0;

        assertCondition(pageNumber, n -> n >= lowerBound,
                format("Page number is out of bounds: %d, indexed from one: %b", pageNumber, oneIndexedParameters));
        assertCondition(pageSize, s -> s <= maxPageSize,
                format("Page size is out of bounds: %d, max page size: %d", pageSize, maxPageSize));

        return PageRequest.of(pageNumber - lowerBound, pageSize);
    }

    private static <T> void assertCondition(T obj, Predicate<T> condition, String errorMessage) {
        if (!condition.test(obj)) {
            throw new PayloadValidationException(errorMessage);
        }
    }
}
