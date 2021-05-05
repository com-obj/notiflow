package com.obj.nc.koderia.functions.processors.recipientsFinder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.obj.nc.utils.JsonUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotBlank;

@Data
@Configuration
@ConfigurationProperties(prefix = "koderia.functions.recipients-finder")
public class KoderiaRecipientsFinderConfig {
    public static final String KODERIA_REST_TEMPLATE = "koderiaRestTemplate";
    public static final String RECIPIENTS_PATH = "/recipients";

    @NotBlank private String koderiaApiUrl;
    @NotBlank private String koderiaApiToken;
    
    @Bean
    @Qualifier(KODERIA_REST_TEMPLATE)
    public RestTemplate koderiaRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        RestTemplate restTemplate = restTemplateBuilder
                .rootUri(koderiaApiUrl)
                .additionalInterceptors(
                        (httpRequest, bytes, clientHttpRequestExecution) -> {
                            httpRequest.getHeaders().add(HttpHeaders.AUTHORIZATION,
                                    "Bearer " + koderiaApiToken);
                            return clientHttpRequestExecution.execute(httpRequest, bytes);
                        })
                .build();
        restTemplate.getMessageConverters().add(0, mappingJacksonHttpMessageConverter());
        return restTemplate;
    }
    
    @Bean
    public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(JsonUtils.getObjectMapper());
        return converter;
    }
}
