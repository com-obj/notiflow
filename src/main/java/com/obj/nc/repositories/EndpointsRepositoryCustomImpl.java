package com.obj.nc.repositories;

import com.obj.nc.domain.dto.EndpointDto;
import com.obj.nc.repositories.mappers.EndpointDtoRowMapper;
import com.obj.nc.utils.QueryUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.obj.nc.domain.dto.EndpointDto.*;
import static com.obj.nc.domain.dto.EndpointDto.EndpointType.ANY;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.SENT;

@RequiredArgsConstructor
public class EndpointsRepositoryCustomImpl implements EndpointsRepositoryCustom {
    
    private final NamedParameterJdbcTemplate jdbcTemplate;
    
    @Override
    public Page<EndpointDto> findAllEndpoints(Instant startAt,
                                              Instant endAt,
                                              EndpointType endpointType,
                                              Pageable pageable) {
        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT DISTINCT ON (ep.id)" +
                        "   ep.id, " +
                        "   ep.endpoint_name, " +
                        "   ep.endpoint_type," +
                        "   sent.count as sent_messages_count " +
                        "FROM " +
                        "   nc_endpoint ep " +
                        "LEFT JOIN " +
                        "   nc_delivery_info di " +
                        "ON " +
                        "   ep.id = di.endpoint_id " +
                        "LEFT JOIN " +
                        "   (SELECT " +
                        "        endpoint_id, " +
                        "        count(status) as count " +
                        "    FROM " +
                        "        nc_delivery_info " +
                        "    WHERE " +
                        "        status = (:statusSent) " +
                        "    GROUP BY " +
                        "        endpoint_id" +
                        "   ) sent " +
                        "ON " +
                        "   ep.id = sent.endpoint_id ");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("statusSent", SENT.toString());
        parameters.putAll(appendQueryFilters(queryBuilder, startAt, endAt, endpointType));
        
        appendQueryPagination(queryBuilder, pageable);
    
        List<EndpointDto> result = jdbcTemplate.query(queryBuilder.toString(), parameters, new EndpointDtoRowMapper());
    
        long totalCount = countAllEndpoints(startAt, endAt, endpointType);
        return new PageImpl<>(result, pageable, totalCount);
    }
    
    private long countAllEndpoints(Instant startAt,
                                   Instant endAt,
                                   EndpointType endpointType) {
        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT count(DISTINCT ep.id) " +
                        "FROM " +
                        "   nc_endpoint ep " +
                        "LEFT JOIN " +
                        "   nc_delivery_info di " +
                        "ON " +
                        "   ep.id = di.endpoint_id " +
                        "LEFT JOIN " +
                        "   (SELECT " +
                        "        endpoint_id, " +
                        "        count(status) as count " +
                        "    FROM " +
                        "        nc_delivery_info " +
                        "    WHERE " +
                        "        status = (:statusSent) " +
                        "    GROUP BY " +
                        "        endpoint_id" +
                        "   ) sent " +
                        "ON " +
                        "   ep.id = sent.endpoint_id ");
    
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("statusSent", SENT.toString());
        parameters.putAll(appendQueryFilters(queryBuilder, startAt, endAt, endpointType));
    
        return jdbcTemplate.queryForObject(queryBuilder.toString(), parameters, Long.class);
    }
    
    private Map<String, Object> appendQueryFilters(StringBuilder queryBuilder,
                                                   Instant startAt,
                                                   Instant endAt,
                                                   EndpointType endpointType) {
        Map<String, Object> parameters = new HashMap<>();
        
        if (ANY.equals(endpointType)) {
            queryBuilder.append("WHERE true");
        } else {
            queryBuilder.append("WHERE ep.endpoint_type = (:endpointType)");
            parameters.put("endpointType", endpointType.toString());
        }
        
        if (startAt != null) {
            queryBuilder.append(" AND di.processed_on >= (:startAt)");
            parameters.put("startAt", Timestamp.from(startAt));
        }
        
        if (endAt != null) {
            queryBuilder.append(" AND di.processed_on <= (:endAt)");
            parameters.put("endAt", Timestamp.from(endAt));
        }
        
        return parameters;
    }
    
    private void appendQueryPagination(StringBuilder queryBuilder, Pageable pageable) {
        queryBuilder.append(" ").append(QueryUtils.toPaginationString(pageable)).append(";");
    }

}
