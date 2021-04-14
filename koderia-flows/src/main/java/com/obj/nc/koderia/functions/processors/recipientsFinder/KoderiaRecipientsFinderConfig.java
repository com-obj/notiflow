package com.obj.nc.koderia.functions.processors.recipientsFinder;

import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotBlank;

@Data
@Configuration
@ConfigurationProperties(prefix = "koderia.functions.recipients")
public class KoderiaRecipientsFinderConfig {
    public static final String KODERIA_REST_TEMPLATE = "koderiaRestTemplate";
    public static final String RECIPIENTS_PATH = "/recipients";

    @NotBlank private String koderiaApiUrl;
    @NotBlank private String koderiaApiToken;
    
    @Bean
    @Qualifier(KODERIA_REST_TEMPLATE)
    public RestTemplate koderiaRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(koderiaApiUrl)
                .additionalInterceptors(
                        (httpRequest, bytes, clientHttpRequestExecution) -> {
                            httpRequest.getHeaders().add(HttpHeaders.AUTHORIZATION,
                                    "Bearer " + koderiaApiToken);
                            return clientHttpRequestExecution.execute(httpRequest, bytes);
                        })
                .build();
    }
}
