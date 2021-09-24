package com.obj.nc.flows.dataSources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.flows.dataSources.DataSourcesConfigurationProperties.ExpiryCheck;
import com.obj.nc.flows.dataSources.DataSourcesConfigurationProperties.JdbcDataSource;
import com.obj.nc.flows.dataSources.DataSourcesConfigurationProperties.Job;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersister;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.integration.dsl.Pollers.cron;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class DataSourcesConfiguration {
    
    private final DataSourcesConfigurationProperties dataSourcesProperties;
    private final IntegrationFlowContext flowContext;
    private final GenericEventPersister genericEventPersister;
    
    @PostConstruct
    public void initCustomDataSources() {
        List<CustomDataSource> customDataSources = dataSourcesProperties
                .getJdbc()
                .stream()
                .map(dataSourceProperties -> {
                    DataSource dataSource = DataSourceBuilder
                            .create()
                            .url(dataSourceProperties.getUrl())
                            .username(dataSourceProperties.getUsername())
                            .password(dataSourceProperties.getPassword())
                            .build();
                    
                    return CustomDataSource
                            .builder()
                            .properties(dataSourceProperties)
                            .dataSource(dataSource)
                            .build();
                })
                .collect(Collectors.toList());
    
        customDataSources.forEach(customDataSource -> {
            customDataSource
                    .getProperties()
                    .getJobs()
                    .forEach(job -> {
                        JdbcPollingChannelAdapter jdbcPollingChannelAdapter = new JdbcPollingChannelAdapter(
                                customDataSource.getDataSource(),
                                createJobQuery(job));
    
                        flowContext
                                .registration(createJobIntegrationFlow(job, jdbcPollingChannelAdapter))
                                .id(createJobFlowId(job))
                                .register();
                    });
        });
    }
    
    private IntegrationFlow createJobIntegrationFlow(Job job, JdbcPollingChannelAdapter jdbcPollingChannelAdapter) {
        return IntegrationFlows
                .from(jdbcPollingChannelAdapter, c -> c
                        .poller(cron(job.getCron()))
                        .id(createJobFlowId(job).concat("_POLLER")))
                .transform(resultSet -> JsonNodeFactory.instance
                        .objectNode()
                        .putPOJO("resultSet", resultSet)
                        .put("description", job.getDescription())
                        .put("templatePath", job.getTemplatePath()))
                .transform(objectNode -> GenericEvent
                        .builder()
                        .id(UUID.randomUUID())
                        .flowId(job.getFullName())
                        .payloadJson((JsonNode) objectNode)
                        .build())
                .handle(genericEventPersister)
                .get();
    }
    
    private String createJobQuery(Job job) {
        String query = String.format("select * from %s", job.getTableName());
        
        ExpiryCheck expiryCheck = job.getExpiryCheck();
        
        if (expiryCheck != null) {
            query = query.concat(String.format(" where %s <= '%s'", 
                    expiryCheck.getColumnName(), 
                    Timestamp.from(Instant.now().plus(expiryCheck.getDaysUntilExpiry(), ChronoUnit.DAYS))));
        }
        
        return query;
    }
    
    private String createJobFlowId(Job job) {
        return "NC_CUSTOM_DATA_SOURCE_"
                .concat(job.getFullName())
                .concat("_INTEGRATION_FLOW");
    }
    
    @Data
    @Builder
    static class CustomDataSource {
        private JdbcDataSource properties;
        private DataSource dataSource;
    }
    
}
