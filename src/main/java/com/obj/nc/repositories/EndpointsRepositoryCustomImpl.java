package com.obj.nc.repositories;

import static com.obj.nc.domain.dto.EndpointDto.EndpointType.ANY;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.SENT;
import static java.sql.Timestamp.from;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.obj.nc.domain.dto.EndpointDto;
import com.obj.nc.domain.dto.EndpointDto.EndpointType;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.repositories.mappers.EndpointDtoRowMapper;
import com.obj.nc.repositories.mappers.ReceivingEndpointRowMapper;
import com.obj.nc.utils.QueryUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class EndpointsRepositoryCustomImpl implements EndpointsRepositoryCustom {
    
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    /**
     *
     * @param toPersist
     * @return client must use returned list because IDs of endpoints can be different to those provided in input parameter
     */
    @Override
    public <T extends ReceivingEndpoint> List<T> persistEnpointIfNotExists(List<T> toPersist) {
        try  {
            NewAndExistingEndpoints newAndExisting = findNewAndExisting(toPersist);
            
            String inserEndpointIfNotExistsSQL =
                    "insert into nc_endpoint "
                            + "(id, endpoint_name, endpoint_type, time_created) "
                            + "values "
                            + "(?, ?, ?, ?) ";
            
            List<ReceivingEndpoint> toInsert = newAndExisting.newEndpoints;
            List<T> persited = (List<T>)newAndExisting.existingEndpoints;
            
            jdbcTemplate.batchUpdate(
                    inserEndpointIfNotExistsSQL,
                    new BatchPreparedStatementSetter() {
                        
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ReceivingEndpoint endpoint = toInsert.get(i);
                            ps.setObject(1, endpoint.getId());
                            ps.setString(2, endpoint.getEndpointId());
                            ps.setString(3, endpoint.getEndpointType());
                            ps.setTimestamp(4, from(Instant.now()));
                            
                            persited.add((T)endpoint);
                        }
                        
                        public int getBatchSize() {
                            return toInsert.size();
                        }
                        
                    });
            
            return persited;
        } catch (RuntimeException e) {
            log.error(e);
            throw e;
        }
    }
    
    private <T extends ReceivingEndpoint> NewAndExistingEndpoints findNewAndExisting(List<T> toPersist) {
        NewAndExistingEndpoints newAndExisting = new NewAndExistingEndpoints();
        
        List<T> existing = findExistingEndpointsByNameId(toPersist);
        for (ReceivingEndpoint endPoint: toPersist) {
            if (!endPoint.isNew()) {
                newAndExisting.existingEndpoints.add(endPoint);
                continue;
            }
            
            Optional<T> existingEndp = existing
                    .stream()
                    .filter(e -> e.getEndpointId().equals(endPoint.getEndpointId()))
                    .findFirst();
            if (existingEndp.isPresent()) {
                newAndExisting.existingEndpoints.add(existingEndp.get());
                continue;
            }
            
            newAndExisting.newEndpoints.add(endPoint);
        }
        return newAndExisting;
    }
    
    private static class NewAndExistingEndpoints {
        public List<ReceivingEndpoint> existingEndpoints = new ArrayList<ReceivingEndpoint>();
        public List<ReceivingEndpoint> newEndpoints = new ArrayList<ReceivingEndpoint>();
    }
    
    
    @Override
    public Map<String, ReceivingEndpoint> persistEnpointIfNotExistsMappedToNameId(List<ReceivingEndpoint> toPersist) {
        List<ReceivingEndpoint> persited = persistEnpointIfNotExists(toPersist);
        
        return persited.stream().collect(Collectors.toMap(ReceivingEndpoint::getEndpointId, Function.identity()));
    }
    
    
    @Override
    public <T extends ReceivingEndpoint> List<T> findExistingEndpointsByNameId(List<T> ednpoints) {
        List<String> namesList = ednpoints.stream()
                .map(ReceivingEndpoint::getEndpointId)
                .collect(Collectors.toList());
        
        List<T> existing = (List<T>)findByNameIds(namesList.toArray(new String[0]));
        
        return existing;
    }
    
    @Override
    public <T extends ReceivingEndpoint> List<T> findExistingEndpointsByIsNewFlag(List<T> ednpoints) {
        List<T> existing = ednpoints.stream()
                .filter(e -> !e.isNew())
                .collect(Collectors.toList());
        
        return existing;
    }
    
    @Override
    public List<ReceivingEndpoint> findByIds(UUID ... endpointIds) {
        String query =
                "select id, endpoint_name, endpoint_type "
                        + "from nc_endpoint "
                        + "where id in (%s)";
        String inSql = String.join(",", Collections.nCopies(endpointIds.length, "?"));
        query = String.format(query, inSql);
        
        List<ReceivingEndpoint> endpoints = jdbcTemplate.query(
                query,
                new ReceivingEndpointRowMapper(),
                (Object[])endpointIds
        );
        
        return endpoints;
    }
    
    @Override
    public List<ReceivingEndpoint> findByNameIds(String ... endpointNames) {
        if (endpointNames.length==0) {
            return new ArrayList<ReceivingEndpoint>();
        }
        
        String query =
                "select id, endpoint_name, endpoint_type "
                        + "from nc_endpoint "
                        + "where endpoint_name in (%s)";
        String inSql = String.join(",", Collections.nCopies(endpointNames.length, "?"));
        query = String.format(query, inSql);
        
        List<ReceivingEndpoint> endpoints = jdbcTemplate.query(
                query,
                new ReceivingEndpointRowMapper(),
                (Object[])endpointNames
        );
        
        return endpoints;
    }
    
    @Override
    public <T extends ReceivingEndpoint> T persistEnpointIfNotExists(T ednpoint) {
        List<ReceivingEndpoint> ednpoints = Arrays.asList(ednpoint);
        
        ReceivingEndpoint persisted = persistEnpointIfNotExists(ednpoints).iterator().next();
        return (T)persisted;
    }
    
    @Override
    public List<ReceivingEndpoint> persistEnpointIfNotExists(ReceivingEndpoint ... ednpoints) {
        return persistEnpointIfNotExists(Arrays.asList(ednpoints));
    }
    
    @Override
    public Map<String, ReceivingEndpoint> persistEnpointIfNotExistsMappedToNameId(ReceivingEndpoint ... ednpoints) {
        return persistEnpointIfNotExistsMappedToNameId(Arrays.asList(ednpoints));
    }

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
    
        List<EndpointDto> result = namedParameterJdbcTemplate.query(queryBuilder.toString(), parameters, new EndpointDtoRowMapper());
    
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
    
        return namedParameterJdbcTemplate.queryForObject(queryBuilder.toString(), parameters, Long.class);
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
    
    @Override
    public boolean existsById(UUID id) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM nc_endpoint WHERE id = ?)", Boolean.class, id);
    }
    
}
