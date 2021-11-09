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

package com.obj.nc.flows.dataSources;

import com.obj.nc.flows.dataSources.properties.DataSourceFlowsProperties;
import com.obj.nc.flows.dataSources.properties.http.HttpDataSourceProperties;
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
    private final PollFlowAdapter pollFlowAdapter;

    public HttpDataSourceFlowsConfiguration(DataSourceFlowsProperties dataSourceFlowsProperties,
                                            IntegrationFlowContext integrationFlowContext,
                                            @Qualifier(PURE_REST_TEMPLATE) RestTemplate pureRestTemplate,
                                            RestTemplateBuilder restTemplateBuilder,
                                            PollFlowAdapter pollFlowAdapter) {

        this.dataSourceFlowsProperties = dataSourceFlowsProperties;
        this.integrationFlowContext = integrationFlowContext;
        this.pureRestTemplate = pureRestTemplate;
        this.restTemplateBuilder = restTemplateBuilder;
        this.pollFlowAdapter = pollFlowAdapter;
    }

    @PostConstruct
    public void createFlows() {
        dataSourceFlowsProperties.getHttp().forEach(dataSource -> {
            integrationFlowContext
                    .registration(createJobIntegrationFlow(dataSource))
                    .id(createJobFlowId(dataSource.getName()))
                    .register();
        });

    }

    private IntegrationFlow createJobIntegrationFlow(HttpDataSourceProperties dataSource) {
        String token = dataSource.getToken();
        RestTemplate restTemplate;

        if (token != null && !token.isEmpty()) {
            restTemplate = restTemplateBuilder.additionalInterceptors((request, body, execution) -> {
                request.getHeaders().add("Authorization", "Bearer " + token);
                return execution.execute(request, body);
            }).build();
        } else {
            restTemplate = pureRestTemplate;
        }

        JobConfig jobConfig = new JobConfig();
        jobConfig.externalIdKey = dataSource.getExternalIdKey();
        jobConfig.pojoFCCN = dataSource.getPojoFCCN();
        jobConfig.spelFilterExpression = dataSource.getSpelFilterExpression();

        return pollFlowAdapter.continueFlow(IntegrationFlows.from(
                        () -> new GenericMessage<>(""),
                        c -> c
                                .poller(cron(dataSource.getCron()))
                                .id(createJobPollerId(dataSource.getName())))
                .handle(Http.outboundGateway(dataSource.getUrl(), restTemplate)
                        .httpMethod(HttpMethod.GET)
                        .expectedResponseType(String.class)), jobConfig);
    }

    private String createDataSourceId(String dataSourceName) {
        return "NC_HTTP_DATA_SOURCE_".concat(dataSourceName);
    }

    private String createJobFlowId(String dataSourceName) {
        return createDataSourceId(dataSourceName).concat("_INTEGRATION_FLOW");
    }

    private String createJobPollerId(String dataSourceName) {
        return createJobFlowId(dataSourceName).concat("_POLLER");
    }
}
