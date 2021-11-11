package com.obj.nc.flows.dataSources;

import com.obj.nc.Get;
import com.obj.nc.flows.dataSources.properties.DataSourceFlowsProperties;
import com.obj.nc.flows.dataSources.properties.jdbc.JdbcDataSourceProperties;
import com.obj.nc.flows.dataSources.properties.jdbc.JdbcJobProperties;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.integration.dsl.Pollers.cron;

@Configuration
@RequiredArgsConstructor
public class JdbcDataSourceFlowsConfiguration {

    private final DataSourceFlowsProperties dataSourceFlowsProperties;
    private final IntegrationFlowContext integrationFlowContext;
    private final Get get;
    private final GenericDataTransformationAndPersistFlow pollFlowAdapter;

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
                                job.getSqlQuery());

                        integrationFlowContext
                                .registration(
                                        createJobIntegrationFlow(
                                                jdbcPollingChannelAdapter,
                                                customDataSource.getProperties().getName(),
                                                job)
                                )
                                .id(createJobFlowId(customDataSource.getProperties().getName(), job.getName()))
                                .register();
                    });
        });
    }

    private IntegrationFlow createJobIntegrationFlow(JdbcPollingChannelAdapter jdbcPollingChannelAdapter,
                                                     String dataSourceName,
                                                     JdbcJobProperties job) {
        JobConfig jobConfig = new JobConfig();
        jobConfig.setExternalIdAttrName(job.getExternalIdColumnName());
        jobConfig.setPojoFCCN(job.getPojoFCCN());
        jobConfig.setSpelFilterExpression(job.getSpelFilterExpression());

        return pollFlowAdapter.continueFlow(IntegrationFlows
                .from(jdbcPollingChannelAdapter, c -> c
                        .poller(cron(job.getCron()))
                        .id(createJobPollerId(dataSourceName, job.getName()))), jobConfig);
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
