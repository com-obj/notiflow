package com.obj.nc.koderia.services;

import com.obj.nc.koderia.functions.processors.senders.MailchimpSenderConfigProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.obj.nc.koderia.dto.mailchimp.MessageResponseDto;
import com.obj.nc.koderia.dto.mailchimp.SendMessageWithTemplateDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class MailchimpRestClientImpl implements MailchimpClient {

    public static final String SEND_TEMPLATE_PATH = "/messages/send-template";

    private final RestTemplate restTemplate;

    public MailchimpRestClientImpl(ResponseErrorHandler responseErrorHandler, MailchimpSenderConfigProperties mailchimpSenderConfigProperties,
                                   RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .rootUri(mailchimpSenderConfigProperties.getMailchimpApi().getUrl())
                .errorHandler(responseErrorHandler)
                .build();
    }

    @Override
    public List<MessageResponseDto> sendMessageWithTemplate(SendMessageWithTemplateDto sendMessageDto) {
        ResponseEntity<MessageResponseDto[]> responseEntity = restTemplate.postForEntity(SEND_TEMPLATE_PATH, sendMessageDto, MessageResponseDto[].class);
        MessageResponseDto[] responseBody = responseEntity.getBody();

        if (responseBody == null) {
            throw new RestClientException("Response body is null");
        }

        return Arrays.stream(responseBody).collect(Collectors.toList());
    }

    @Override
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

}
