package com.obj.nc.flows.dataSources;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.dataObject.GenericData;
import com.obj.nc.flows.dataSources.DataSourceFlowsProperties.ExpiryCheck;
import com.obj.nc.flows.dataSources.DataSourceFlowsProperties.JdbcDataSource;
import com.obj.nc.flows.dataSources.DataSourceFlowsProperties.Job;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.integration.json.ObjectToJsonTransformer.ResultType;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.obj.nc.flows.dataSources.GenericDataConvertingFlowConfiguration.GENERIC_DATA_CONVERTING_FLOW_ID_INPUT_CHANNEL_ID;
import static org.springframework.integration.dsl.Pollers.cron;

@Configuration
@RequiredArgsConstructor
public class DataSourceFlowsConfiguration {
    
    private final DataSourceFlowsProperties dataSourceFlowsProperties;
    private final IntegrationFlowContext integrationFlowContext;
    
    @PostConstruct
    public void initCustomDataSources() {
        List<CustomDataSource> customDataSources = dataSourceFlowsProperties
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
    
                        integrationFlowContext
                                .registration(createJobIntegrationFlow(
                                        jdbcPollingChannelAdapter,
                                        customDataSource.getProperties().getName(),
                                        job.getName(),
                                        job.getCron()))
                                .id(createJobFlowId(customDataSource.getProperties().getName(), job.getName()))
                                .register();
                    });
        });
    }
    
    private IntegrationFlow createJobIntegrationFlow(JdbcPollingChannelAdapter jdbcPollingChannelAdapter,
                                                     String dataSourceName,
                                                     String jobName,
                                                     String jobCron) {
        return IntegrationFlows
                .from(jdbcPollingChannelAdapter, c -> c
                        .poller(cron(jobCron))
                        .id(createJobFlowId(dataSourceName, jobName).concat("_POLLER")))
                .transform(Transformers.toJson(ResultType.NODE))
                .split() // split ArrayNode to JsonNode-s
                .aggregate() // aggregate JsonNode-s to List<JsonNode>
                .<List<JsonNode>, GenericData>transform(nodes -> 
                        GenericData
                                .builder()
                                .payloads(nodes)
                                .build())
                .channel(GENERIC_DATA_CONVERTING_FLOW_ID_INPUT_CHANNEL_ID)
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
    
    private String createJobFlowId(String dataSourceName, String jobName) {
        return "NC_CUSTOM_DATA_SOURCE_"
                .concat(dataSourceName)
                .concat(".")
                .concat(jobName)
                .concat("_INTEGRATION_FLOW");
    }
    
    @Data
    @Builder
    static class CustomDataSource {
        private JdbcDataSource properties;
        private DataSource dataSource;
    }
    
}
