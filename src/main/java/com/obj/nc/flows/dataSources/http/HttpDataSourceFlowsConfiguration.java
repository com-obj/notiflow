/*
 * Copyright (C) 2021 the original author or authors.
 * This file is part of Notiflow
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.obj.nc.flows.dataSources.http;

import com.obj.nc.flows.dataSources.GenericDataTransformationAndPersistFlow;
import com.obj.nc.flows.dataSources.JobConfig;
import com.obj.nc.flows.dataSources.http.properties.HttpDataSourceProperties;
import com.obj.nc.flows.dataSources.properties.DataSourceFlowsProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.http.dsl.Http;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import static com.obj.nc.config.PureRestTemplateConfig.PURE_REST_TEMPLATE;
import static org.springframework.integration.dsl.Pollers.cron;

@Configuration
public class HttpDataSourceFlowsConfiguration {
    private final DataSourceFlowsProperties dataSourceFlowsProperties;
    private final IntegrationFlowContext integrationFlowContext;
    private final RestTemplate pureRestTemplate;
    private final RestTemplateBuilder restTemplateBuilder;
    private final GenericDataTransformationAndPersistFlow nextFlow;

    public HttpDataSourceFlowsConfiguration(DataSourceFlowsProperties dataSourceFlowsProperties,
                                            IntegrationFlowContext integrationFlowContext,
                                            @Qualifier(PURE_REST_TEMPLATE) RestTemplate pureRestTemplate,
                                            RestTemplateBuilder restTemplateBuilder,
                                            GenericDataTransformationAndPersistFlow nextFlow) {
        this.dataSourceFlowsProperties = dataSourceFlowsProperties;
        this.integrationFlowContext = integrationFlowContext;
        this.pureRestTemplate = pureRestTemplate;
        this.restTemplateBuilder = restTemplateBuilder;
        this.nextFlow = nextFlow;
    }

    @PostConstruct
    public void createFlows() {
        dataSourceFlowsProperties.getHttp().forEach(dataSource -> {
            integrationFlowContext
                    .registration(createJobIntegrationFlow(dataSource))
                    .id(HttpDatasourceNameCreator.createJobFlowId(dataSource.getName()))
                    .register();
        });

    }

    private IntegrationFlow createJobIntegrationFlow(HttpDataSourceProperties dataSource) {
        JobConfig jobConfig = new JobConfig();
        jobConfig.setExternalIdAttrName(dataSource.getExternalIdAttrName());
        jobConfig.setPojoFCCN(dataSource.getPojoFCCN());
        jobConfig.setSpelFilterExpression(dataSource.getSpelFilterExpression());

        return nextFlow.continueFlow(IntegrationFlows.from(
                        () -> new GenericMessage<>(""),
                        c -> c
                                .poller(cron(dataSource.getCron()))
                                .id(HttpDatasourceNameCreator.createJobPollerId(dataSource.getName())))
                .handle(Http.outboundGateway(dataSource.getUrl(), getRestTemplate(dataSource))
                        .httpMethod(HttpMethod.GET)
                        .expectedResponseType(String.class)), jobConfig);
    }

    private RestTemplate getRestTemplate(HttpDataSourceProperties dataSource) {
        String token = dataSource.getToken();

        if (token == null || token.isEmpty()) {
            return pureRestTemplate;
        }

        return restTemplateBuilder.additionalInterceptors((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + token);
            return execution.execute(request, body);
        }).build();
    }
}
