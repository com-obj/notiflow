package com.obj.nc.service;

import com.obj.nc.dto.SendSmsRequestDto;
import com.obj.nc.dto.SendSmsResponseDto;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsRestClientImpl implements SmsClient {

    private final RestTemplate smsRestTemplate;

    public SmsRestClientImpl(SmsClientConfigProperties properties,
                             RestTemplateBuilder restTemplateBuilder) {
        this.smsRestTemplate = restTemplateBuilder.rootUri(properties.getUri()).build();
    }

    @Override
    public SendSmsResponseDto sendSms(SendSmsRequestDto sendSmsRequestDto) {
        SendSmsResponseDto responseBody = smsRestTemplate.postForEntity(
                SmsRestClientConstants.SEND_PATH,
                sendSmsRequestDto,
                SendSmsResponseDto.class,
                sendSmsRequestDto.getSenderAddress()
        ).getBody();

        if (responseBody == null) {
            throw new RestClientException("Response body is null");
        }

        String resourceURL = responseBody.getResourceReference().getResourceURL();

        if (resourceURL.contains(SmsRestClientConstants.STATUS_SUCCESS)) {
            return responseBody;
        } else if (resourceURL.contains(SmsRestClientConstants.STATUS_FAILURE)) {
            throw new RuntimeException(resourceURL);
        } else {
            throw new RuntimeException("Unknown response status");
        }
    }

}
