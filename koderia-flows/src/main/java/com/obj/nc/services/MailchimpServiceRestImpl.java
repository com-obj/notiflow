package com.obj.nc.services;

import com.obj.nc.dto.mailchimp.MessageResponseDto;
import com.obj.nc.dto.mailchimp.SendMessageWithTemplateDto;
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

import static com.obj.nc.config.MailchimpApiConfig.MAILCHIMP_REST_TEMPLATE;

@Service
@Log4j2
public class MailchimpServiceRestImpl implements MailchimpService {

    public static final String SEND_TEMPLATE_PATH = "/messages/send-template";

    @Autowired
    @Qualifier(MAILCHIMP_REST_TEMPLATE)
    private RestTemplate restTemplate;

    @Override
    public List<MessageResponseDto> sendMessageWithTemplate(SendMessageWithTemplateDto sendMessageDto) {
        ResponseEntity<MessageResponseDto[]> responseEntity = restTemplate.postForEntity(SEND_TEMPLATE_PATH, sendMessageDto, MessageResponseDto[].class);
        MessageResponseDto[] responseBody = responseEntity.getBody();

        if (responseBody == null) {
            throw new RestClientException("Response body is null");
        }

        return Arrays.stream(responseBody).collect(Collectors.toList());
    }

}
