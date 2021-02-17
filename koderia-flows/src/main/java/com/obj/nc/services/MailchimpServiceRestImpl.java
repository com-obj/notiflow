package com.obj.nc.services;

import com.obj.nc.dto.mailchimp.MessageResponseDto;
import com.obj.nc.dto.mailchimp.SendMessageDto;
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

@Service
@Log4j2
public class MailchimpServiceRestImpl implements MailchimpService {

    public static final String MESSAGES_PATH = "/messages";
    public static final String SEND_PATH = "/send";
    public static final String SEND_TEMPLATE_PATH = "/send-template";

    @Autowired
    @Qualifier("mailchimpRestTemplate")
    private RestTemplate restTemplate;

    @Override
    public List<MessageResponseDto> sendMessage(SendMessageDto sendMessageDto) {
        ResponseEntity<MessageResponseDto[]> responseEntity = restTemplate.postForEntity(MESSAGES_PATH + SEND_PATH, sendMessageDto, MessageResponseDto[].class);
        MessageResponseDto[] responseBody = responseEntity.getBody();

        if (responseBody == null) {
            throw new RestClientException("Response body is null");
        }

        return Arrays.stream(responseBody).collect(Collectors.toList());
    }

    @Override
    public List<MessageResponseDto> sendMessageWithTemplate(SendMessageWithTemplateDto message) {
        ResponseEntity<MessageResponseDto[]> responseEntity = restTemplate.postForEntity(MESSAGES_PATH + SEND_TEMPLATE_PATH, message, MessageResponseDto[].class);
        MessageResponseDto[] responseBody = responseEntity.getBody();

        if (responseBody == null) {
            throw new RestClientException("Response body is null");
        }

        return Arrays.stream(responseBody).collect(Collectors.toList());
    }

}
