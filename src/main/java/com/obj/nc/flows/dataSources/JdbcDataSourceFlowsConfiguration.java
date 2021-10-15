package com.obj.nc.flows.dataSources;

import com.obj.nc.Get;
import com.obj.nc.flows.dataSources.properties.DataSourceFlowsProperties;
import com.obj.nc.flows.dataSources.properties.jdbc.JdbcDataSourceProperties;
import com.obj.nc.flows.dataSources.properties.jdbc.JdbcJob;
import com.obj.nc.functions.processors.jsonNodeToGenericDataTransformer.JsonNodeToGenericDataTransformer;
import com.obj.nc.utils.JsonUtils;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.*;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.integration.json.ObjectToJsonTransformer.ResultType;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

import static com.obj.nc.flows.dataSources.GenericDataConvertingFlowConfiguration.GENERIC_DATA_CONVERTING_FLOW_ID_INPUT_CHANNEL_ID;
import static org.springframework.integration.dsl.Pollers.cron;

@Configuration
@RequiredArgsConstructor
public class JdbcDataSourceFlowsConfiguration {
    
    private final DataSourceFlowsProperties dataSourceFlowsProperties;
    private final IntegrationFlowContext integrationFlowContext;
    private final Get get;
    
    @PostConstruct
    public void createJdbcDataSourceFlows() {
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
                    
                    get.registerBean(createDataSourceId(dataSourceProperties.getName()), DataSource.class);
                    
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
                                        job))
                                .id(createJobFlowId(customDataSource.getProperties().getName(), job.getName()))
                                .register();
                    });
        });
    }
    
    private IntegrationFlow createJobIntegrationFlow(JdbcPollingChannelAdapter jdbcPollingChannelAdapter,
                                                     String dataSourceName,
                                                     JdbcJob job) {
        return IntegrationFlows
                .from(jdbcPollingChannelAdapter, c -> c
                        .poller(cron(job.getCron()))
                        .id(createJobPollerId(dataSourceName, job.getName())))
                .transform(Transformers.toJson(JsonUtils.getJsonObjectMapper(), ResultType.NODE))
                .split() // split ArrayNode to JsonNode-s
                .aggregate() // aggregate JsonNode-s to List<JsonNode>
                .handle(new JsonNodeToGenericDataTransformer(job.getPojoFCCN()))
                .channel(GENERIC_DATA_CONVERTING_FLOW_ID_INPUT_CHANNEL_ID)
                .get();
    }
    
    private String createJobQuery(JdbcJob job) {
        // String query = String.format("select * from %s", job.getEntityName());
        
        // if (job.isExpiryCheckJob()) {
        //     ExpiryCheck expiryCheck = job.getExpiryCheck();
            
        //     query = query.concat(String.format(" where %s <= '%s'", 
        //             expiryCheck.getFieldName(), 
        //             Timestamp.from(Instant.now().plus(expiryCheck.getDaysUntilExpiry(), ChronoUnit.DAYS))));
        // }
        
        return job.getSqlQuery();
    }
    
    private String createDataSourceId(String dataSourceName) {
        return "NC_JDBC_DATA_SOURCE_".concat(dataSourceName);
    }
    
    private String createJobFlowId(String dataSourceName, String jobName) {
        return createDataSourceId(dataSourceName)
                .concat("_")
                .concat(jobName)
                .concat("_INTEGRATION_FLOW");
    }
    
    private String createJobPollerId(String dataSourceName, String jobName) {
        return createJobFlowId(dataSourceName, jobName)
                .concat("_POLLER");
    }
    
    @Data
    @Builder
    static class CustomDataSource {
        private JdbcDataSourceProperties properties;
        private DataSource dataSource;
    }
    
}
