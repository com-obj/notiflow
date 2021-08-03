package com.obj.nc.repositories;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.repositories.mappers.ReceivingEndpointRowMapper;
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

@RequiredArgsConstructor
public class EndpointsRepositoryCustomImpl implements EndpointsRepositoryCustom {
    
    private final NamedParameterJdbcTemplate jdbcTemplate;
    
    @Override
    public Page<RecievingEndpoint> findAllEndpoints(Instant startAt,
                                                    Instant endAt,
                                                    EndpointType endpointType,
                                                    Pageable pageable) {
        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT DISTINCT ON (ep.id)" +
                        "   ep.id, " +
                        "   ep.endpoint_name, " +
                        "   ep.endpoint_type " +
                        "FROM " +
                        "   nc_endpoint ep " +
                        "LEFT JOIN " +
                        "   nc_delivery_info di " +
                        "ON " +
                        "   ep.id = di.endpoint_id ");
        
        Map<String, Object> parameters = appendQueryFilters(queryBuilder, startAt, endAt, QueryUtils.toLikeFilter(endpointType));
        appendPagination(queryBuilder, pageable);
    
        List<RecievingEndpoint> result = jdbcTemplate.query(queryBuilder.toString(), parameters, new ReceivingEndpointRowMapper());
    
        long totalCount = countAllEndpoints(startAt, endAt, endpointType);
        return new PageImpl<>(result, pageable, totalCount);
    }
    
    private void appendPagination(StringBuilder queryBuilder, Pageable pageable) {
        queryBuilder.append(" ").append(QueryUtils.toPaginationString(pageable)).append(";");
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
                        "   ep.id = di.endpoint_id ");
    
        Map<String, Object> parameters = appendQueryFilters(queryBuilder, startAt, endAt, QueryUtils.toLikeFilter(endpointType));
    
        return jdbcTemplate.queryForObject(queryBuilder.toString(), parameters, Long.class);
    }
    
    private Map<String, Object> appendQueryFilters(StringBuilder queryBuilder,
                                                   Instant startAt,
                                                   Instant endAt,
                                                   String endpointType) {
        Map<String, Object> parameters = new HashMap<>();
    
        queryBuilder.append("WHERE ep.endpoint_type LIKE (:endpointType)");
        parameters.put("endpointType", endpointType);
        
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

}
