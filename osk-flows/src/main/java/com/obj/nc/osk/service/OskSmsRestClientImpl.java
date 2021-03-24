package com.obj.nc.osk.service;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SimpleText;
import com.obj.nc.osk.dto.SendSmsResourceReferenceDto;
import com.obj.nc.osk.dto.OskSendSmsRequestDto;
import com.obj.nc.osk.dto.OskSendSmsResponseDto;
import com.obj.nc.osk.exception.SmsClientException;
import com.obj.nc.osk.functions.senders.OskSmsSenderConfigProperties;
import com.obj.nc.services.SmsClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.stream.Collectors;

@Service
@Validated
public class OskSmsRestClientImpl implements SmsClient<OskSendSmsRequestDto, OskSendSmsResponseDto> {

    private final OskSmsSenderConfigProperties properties;
    private final RestTemplate smsRestTemplate;

    public OskSmsRestClientImpl(OskSmsSenderConfigProperties properties,
                                RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.smsRestTemplate = restTemplateBuilder.rootUri(properties.getGapApiUrl())
                .basicAuthentication(properties.getGapApiLogin(), properties.getGapApiPassword()).build();
    }

    @Override
    public OskSendSmsRequestDto convertMessage(Message message) {
        OskSendSmsRequestDto result = new OskSendSmsRequestDto();

        result.setAddress(message.getBody().getRecievingEndpoints().stream()
                .map(RecievingEndpoint::getEndpointId)
                .collect(Collectors.toList()));

        result.setClientCorrelator(properties.getClientCorrelatorPrefix());

        SimpleText content = message.getBody().getContentTyped();
        result.setMessage(content.getText());

        result.setNotifyURL(properties.getNotifyUrl());
        result.setSenderAddress(properties.getSenderAddress());
        result.setBillCode(properties.getBillCode());

        return result;
    }

    @Override
    public OskSendSmsResponseDto send(@Valid @NotNull OskSendSmsRequestDto oskSendSmsRequestDto) {
        OskSendSmsResponseDto responseBody = smsRestTemplate.postForEntity(
                SmsRestClientConstants.SEND_PATH,
                oskSendSmsRequestDto,
                OskSendSmsResponseDto.class,
                oskSendSmsRequestDto.getSenderAddress()).getBody();

        if (responseBody == null) {
            throw new RestClientException("Sms response body must not be null");
        }

        SendSmsResourceReferenceDto resourceReference = responseBody.getResourceReference();

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
