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
