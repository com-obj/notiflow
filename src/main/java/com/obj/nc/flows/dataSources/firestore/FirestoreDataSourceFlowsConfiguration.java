package com.obj.nc.flows.dataSources.firestore;

import com.google.cloud.firestore.Firestore;
import com.obj.nc.flows.dataSources.DataSourceFlowsProperties;
import com.obj.nc.flows.dataSources.JobConfig;
import com.obj.nc.flows.dataSources.PullNotifDataTransformationAndPersistFlow;
import com.obj.nc.flows.dataSources.firestore.properties.FirestoreDataSourceProperties;
import com.obj.nc.flows.dataSources.firestore.properties.FirestoreJobProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;

import javax.annotation.PostConstruct;

import java.util.concurrent.TimeUnit;

import static org.springframework.integration.dsl.Pollers.cron;
import static org.springframework.integration.dsl.Pollers.fixedDelay;

@Configuration
public class FirestoreDataSourceFlowsConfiguration {
    private final DataSourceFlowsProperties dataSourceFlowsProperties;
    private final IntegrationFlowContext integrationFlowContext;
    private final PullNotifDataTransformationAndPersistFlow nextFlow;
    private final FirestoreFactory firestoreFactory;

    public FirestoreDataSourceFlowsConfiguration(DataSourceFlowsProperties dataSourceFlowsProperties,
                                                 IntegrationFlowContext integrationFlowContext,
                                                 PullNotifDataTransformationAndPersistFlow nextFlow,
                                                 FirestoreFactory firestoreFactory) {
        this.dataSourceFlowsProperties = dataSourceFlowsProperties;
        this.integrationFlowContext = integrationFlowContext;
        this.nextFlow = nextFlow;
        this.firestoreFactory = firestoreFactory;
    }

    @PostConstruct
    public void createFlows() {
        dataSourceFlowsProperties.getFirestore().forEach(firestoreDataSourceProperties -> {
            firestoreDataSourceProperties.getJobs().forEach(firestoreJobProperties -> {
                Firestore firestore = firestoreFactory.getFirestore(firestoreDataSourceProperties);
                FirestoreChannelAdapter firestoreChannelAdapter = new FirestoreChannelAdapter(firestore, firestoreJobProperties);

                integrationFlowContext
                        .registration(
                                createJobIntegrationFlow(
                                        firestoreChannelAdapter,
                                        firestoreDataSourceProperties,
                                        firestoreJobProperties)
                        )
                        .id(createJobFlowId(firestoreDataSourceProperties.getName(), firestoreJobProperties.getName()))
                        .register();
            });
        });

    }

    private IntegrationFlow createJobIntegrationFlow(FirestoreChannelAdapter firestoreChannelAdapter,
                                                     FirestoreDataSourceProperties dataSourceProperties,
                                                     FirestoreJobProperties jobProperties) {
        JobConfig jobConfig = new JobConfig();
        jobConfig.setExternalIdAttrName(jobProperties.getExternalIdAttrName());
        jobConfig.setPojoFCCN(jobProperties.getPojoFCCN());
        jobConfig.setSpelFilterExpression(jobProperties.getSpelFilterExpression());

        return nextFlow.continueFlow(IntegrationFlows.from(
                firestoreChannelAdapter,
                consumer -> consumer
                        .poller(cron(jobProperties.getCron()))
                        .id(createJobPollerId(dataSourceProperties.getName(), jobProperties.getName()))), jobConfig);
    }

    private String createDataSourceId(String dataSourceName) {
        return "NC_FIRESTORE_DATA_SOURCE_".concat(dataSourceName);
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
}
