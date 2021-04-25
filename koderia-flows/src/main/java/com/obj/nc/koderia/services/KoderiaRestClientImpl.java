package com.obj.nc.koderia.services;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.koderia.config.KoderiaApiConfigProperties;
import com.obj.nc.koderia.dto.RecipientDto;
import com.obj.nc.koderia.dto.RecipientsQueryDto;
import com.obj.nc.koderia.mapper.RecipientMapper;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class KoderiaRestClientImpl implements KoderiaClient {

    public static final String RECIPIENTS_PATH = "/recipients";

    private final RecipientMapper recipientMapper;

    private final RestTemplate restTemplate;

    public KoderiaRestClientImpl(ResponseErrorHandler responseErrorHandler, RecipientMapper recipientMapper,
                                 KoderiaApiConfigProperties koderiaApiConfigProperties, RestTemplateBuilder restTemplateBuilder) {
        this.recipientMapper = recipientMapper;
        this.restTemplate = restTemplateBuilder
                .rootUri(koderiaApiConfigProperties.getUri())
                .errorHandler(responseErrorHandler)
                .build();
    }

    @Override
    public List<RecievingEndpoint> findReceivingEndpoints(RecipientsQueryDto query) {
        ResponseEntity<RecipientDto[]> responseEntity = restTemplate.postForEntity(RECIPIENTS_PATH, query, RecipientDto[].class);
        RecipientDto[] responseBody = responseEntity.getBody();

        if (responseBody == null) {
            throw new RestClientException("Response body is null");
        }

        return Arrays.stream(responseBody)
                .map(recipientMapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

}
