package com.obj.nc.services;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.dto.RecipientDto;
import com.obj.nc.dto.RecipientsQueryDto;
import com.obj.nc.mapper.RecipientMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.obj.nc.config.KoderiaApiConfig.KODERIA_REST_TEMPLATE;

@Service
@Log4j2
public class KoderiaServiceRestImpl implements KoderiaService {

    public static final String RECIPIENTS_PATH = "/recipients";

    @Autowired
    @Qualifier(KODERIA_REST_TEMPLATE)
    private RestTemplate restTemplate;

    @Autowired
    private RecipientMapper recipientMapper;

    @Override
    public List<EmailEndpoint> findEmailEndpoints(RecipientsQueryDto query) {
        ResponseEntity<RecipientDto[]> responseEntity = restTemplate.postForEntity(RECIPIENTS_PATH, query, RecipientDto[].class);
        RecipientDto[] responseBody = responseEntity.getBody();

        if (responseBody == null) {
            throw new RestClientException("Response body is null");
        }

        return Arrays.stream(responseBody)
                .map(recipientMapper::map)
                .collect(Collectors.toList());
    }

}
