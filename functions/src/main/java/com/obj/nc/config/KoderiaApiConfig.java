package com.obj.nc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Data
@Configuration
@ConfigurationProperties(prefix = "koderia.api")
public class KoderiaApiConfig {

    private String uri;

    @Bean
    public RestTemplate koderiaRestTemplate() {
        return new RestTemplateBuilder()
                .rootUri(uri)
                .errorHandler(responseErrorHandler())
                .build();
    }

    @Bean
    public ResponseErrorHandler responseErrorHandler() {
        return new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
                return (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                        || httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
            }

            @Override
            public void handleError(ClientHttpResponse httpResponse) throws IOException {
                throw new RestClientException(httpResponse.getStatusCode().toString());
            }
        };
    }

}
