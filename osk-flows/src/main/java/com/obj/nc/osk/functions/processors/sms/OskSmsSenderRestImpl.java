package com.obj.nc.osk.functions.processors.sms;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SmstMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.SmsSender;
import com.obj.nc.osk.exception.SmsClientException;
import com.obj.nc.osk.functions.processors.sms.config.OskSmsSenderConfigProperties;
import com.obj.nc.osk.functions.processors.sms.dtos.OskSendSmsRequestDto;
import com.obj.nc.osk.functions.processors.sms.dtos.OskSendSmsResponseDto;
import com.obj.nc.osk.functions.processors.sms.dtos.SendSmsResourceReferenceDto;

@Validated
@DocumentProcessingInfo("GAP_SMSSender")
public class OskSmsSenderRestImpl extends ProcessorFunctionAdapter<SmstMessage, SmstMessage> implements SmsSender {

    public static final String SEND_PATH = "/outbound/{senderAddress}/requests";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";
    
    public static final String SEND_SMS_RESPONSE_ATTRIBUTE = "sendSmsResponse";

    private final OskSmsSenderConfigProperties properties;
    private final RestTemplate restTemplate;

    public OskSmsSenderRestImpl(OskSmsSenderConfigProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder
        		.rootUri(
        			this.properties.getGapApiUrl())
                .basicAuthentication(this.properties.getGapApiLogin(), this.properties.getGapApiPassword())
                .build();
    }
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(SmstMessage payload) {
        if (payload == null) {
            return Optional.of(new PayloadValidationException("Message must not be null"));
        }

        if (payload.getRecievingEndpoints().stream().anyMatch(endpoint -> !(endpoint instanceof SmsEndpoint))) {
            return Optional.of(new PayloadValidationException(String.format("Sms sender can only send message to endpoint of type %s", SmsEndpoint.JSON_TYPE_IDENTIFIER)));
        }

        if (!(payload.getBody() instanceof SimpleTextContent)) {
            return Optional.of(new PayloadValidationException(String.format("Sms sender can only send message with content of type %s", payload.getBody().getClass())));
        }

        return Optional.empty();
    }
    
	@Override
	protected SmstMessage execute(SmstMessage payload) {
		OskSendSmsRequestDto req = convertMessageToRequest(payload);
		OskSendSmsResponseDto resp = sendRequest(req);
        payload.getBody().setAttributeValue(SEND_SMS_RESPONSE_ATTRIBUTE, resp);
        return payload;
	}



    public OskSendSmsRequestDto convertMessageToRequest(Message<SimpleTextContent> message) {
        OskSendSmsRequestDto result = new OskSendSmsRequestDto();

        result.setAddress(message.getRecievingEndpoints().stream()
                .map(RecievingEndpoint::getEndpointId)
                .collect(Collectors.toList()));
    
        ZonedDateTime zdt = ZonedDateTime.now();
        result.setClientCorrelator(properties.getClientCorrelatorPrefix() + "-" +  DateTimeFormatter.ISO_INSTANT.format(zdt));

        SimpleTextContent content = message.getBody();
        result.setMessage(content.getText());

        result.setNotifyURL(properties.getNotifyUrl());
        result.setSenderAddress(properties.getSenderAddress());
        result.setBillCode(properties.getBillCode());

        return result;
    }

    public OskSendSmsResponseDto sendRequest(@Valid @NotNull OskSendSmsRequestDto oskSendSmsRequestDto) {
        OskSendSmsResponseDto responseBody = restTemplate.postForEntity(
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

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}



}
