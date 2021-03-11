package com.obj.nc.service;

import com.obj.nc.dto.ResourceReferenceDto;
import com.obj.nc.dto.SendSmsRequestDto;
import com.obj.nc.dto.SendSmsResponseDto;
import com.obj.nc.exception.SmsClientException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.*;
import javax.validation.constraints.NotNull;

@Service
@Validated
public class SmsRestClientImpl implements SmsClient {

    private final RestTemplate smsRestTemplate;

    public SmsRestClientImpl(SmsClientConfigProperties properties,
                             RestTemplateBuilder restTemplateBuilder) {
        this.smsRestTemplate = restTemplateBuilder.rootUri(properties.getUri()).build();
    }

    @Override
    public SendSmsResponseDto sendSms(@Valid @NotNull SendSmsRequestDto sendSmsRequestDto) {
        SendSmsResponseDto responseBody = smsRestTemplate.postForEntity(
                SmsRestClientConstants.SEND_PATH,
                sendSmsRequestDto,
                SendSmsResponseDto.class,
                sendSmsRequestDto.getSenderAddress()
        ).getBody();

        if (responseBody == null) {
            throw new RestClientException("Sms response body must not be null");
        }

        ResourceReferenceDto resourceReference = responseBody.getResourceReference();

        if (resourceReference == null) {
            throw new RestClientException("Resource reference must not be null");
        }

        String resourceURL = resourceReference.getResourceURL();

        if (resourceURL == null) {
            throw new RestClientException("Resource URL must not be null");
        }

        if (resourceURL.contains(SmsRestClientConstants.STATUS_SUCCESS)) {
            return responseBody;
        } else if (resourceURL.contains(SmsRestClientConstants.STATUS_FAILURE)) {
            throw new SmsClientException(resourceURL);
        } else {
            throw new SmsClientException("Unknown response status");
        }
    }

}
