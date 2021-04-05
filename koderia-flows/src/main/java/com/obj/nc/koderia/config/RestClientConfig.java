package com.obj.nc.koderia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;

import java.io.IOException;

@Configuration
public class RestClientConfig {

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
