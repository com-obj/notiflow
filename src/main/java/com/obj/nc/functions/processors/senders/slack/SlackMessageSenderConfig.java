/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.functions.processors.senders.slack;

import com.obj.nc.config.SlackConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SlackMessageSenderConfig {
    public static final String SLACK_REST_TEMPLATE = "slackRestTemplate";

    @Autowired
    private SlackConfiguration config;

    @Bean
    @Qualifier(SLACK_REST_TEMPLATE)
    public RestTemplate slackRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(config.getApiUrl())
                .additionalInterceptors(
                        (httpRequest, body, execution) -> {
                            httpRequest.getHeaders().setBearerAuth(config.getBotToken());
                            return execution.execute(httpRequest, body);
                        })
                .build();
    }
}
