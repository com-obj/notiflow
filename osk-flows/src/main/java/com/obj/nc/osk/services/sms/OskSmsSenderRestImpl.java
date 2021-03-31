package com.obj.nc.osk.services.sms;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.osk.exception.SmsClientException;
import com.obj.nc.osk.services.sms.config.OskSmsSenderConfigProperties;
import com.obj.nc.osk.services.sms.dtos.OskSendSmsRequestDto;
import com.obj.nc.osk.services.sms.dtos.OskSendSmsResponseDto;
import com.obj.nc.osk.services.sms.dtos.SendSmsResourceReferenceDto;
import com.obj.nc.services.SmsSenderExcecution;

@Service
@Validated
public class OskSmsSenderRestImpl implements SmsSenderExcecution<OskSendSmsResponseDto> {

    public static final String SEND_PATH = "/outbound/{senderAddress}/requests";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";

    private final OskSmsSenderConfigProperties properties;
    private final RestTemplate smsRestTemplate;

    public OskSmsSenderRestImpl(OskSmsSenderConfigProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.smsRestTemplate = restTemplateBuilder
        		.rootUri(
        			this.properties.getGapApiUrl())
                .basicAuthentication(this.properties.getGapApiLogin(), this.properties.getGapApiPassword())
                .build();
    }
    
	@Override
	public OskSendSmsResponseDto apply(Message message) {
		OskSendSmsRequestDto req = convertMessageToRequest(message);
		return sendRequest(req);
	}

    public OskSendSmsRequestDto convertMessageToRequest(Message message) {
        OskSendSmsRequestDto result = new OskSendSmsRequestDto();

        result.setAddress(message.getBody().getRecievingEndpoints().stream()
                .map(RecievingEndpoint::getEndpointId)
                .collect(Collectors.toList()));
    
        ZonedDateTime zdt = ZonedDateTime.now();
        result.setClientCorrelator(properties.getClientCorrelatorPrefix() + "-" +  DateTimeFormatter.ISO_INSTANT.format(zdt));

        SimpleTextContent content = message.getBody().getContentTyped();
        result.setMessage(content.getText());

        result.setNotifyURL(properties.getNotifyUrl());
        result.setSenderAddress(properties.getSenderAddress());
        result.setBillCode(properties.getBillCode());

        return result;
    }

    public OskSendSmsResponseDto sendRequest(@Valid @NotNull OskSendSmsRequestDto oskSendSmsRequestDto) {
        OskSendSmsResponseDto responseBody = smsRestTemplate.postForEntity(
                SEND_PATH,
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

        if (resourceURL.contains(STATUS_SUCCESS)) {
            return responseBody;
        } else if (resourceURL.contains(STATUS_FAILURE)) {
            throw new SmsClientException(resourceURL);
        } else {
            throw new SmsClientException("Unknown response status");
        }
    }



}
