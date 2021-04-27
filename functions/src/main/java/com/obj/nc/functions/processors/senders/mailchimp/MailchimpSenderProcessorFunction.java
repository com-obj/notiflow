package com.obj.nc.functions.processors.senders.mailchimp;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.functions.processors.senders.mailchimp.model.MailchimpContentDto;
import com.obj.nc.domain.content.mailchimp.MailchimpRecipient;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import com.obj.nc.functions.processors.senders.mailchimp.model.MailchimpResponseDto;
import com.obj.nc.functions.processors.senders.MailchimpSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig.*;

@DocumentProcessingInfo("SendMailchimpMessage")
public class MailchimpSenderProcessorFunction extends ProcessorFunctionAdapter<Message, Message> implements MailchimpSender {
	
	@Qualifier(MAILCHIMP_REST_TEMPLATE)
	@Autowired private RestTemplate restTemplate;
	@Autowired private MailchimpSenderConfig mailchimpSenderConfig;

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Message must not be null"));
		}
		
		if (payload.getContentTyped() == null) {
			return Optional.of(new PayloadValidationException("Message content must not be null"));
		}
		
		boolean hasNoneOrTooMuchEndpoints = payload.getBody().getRecievingEndpoints().size() != 1;
		boolean containsNonEmailEndpoint = payload.getBody().getRecievingEndpoints().stream()
				.anyMatch(endpoint -> !MailchimpEndpoint.JSON_TYPE_IDENTIFIER.equals(endpoint.getEndpointType()));
		
		if (hasNoneOrTooMuchEndpoints || containsNonEmailEndpoint) {
			return Optional.of(new PayloadValidationException(String.format("MailchimpSender can only send message %s to 1 MailchimpEndpoint", payload)));
		}
		
		return Optional.empty();
	}

	@Override
	protected Message execute(Message payload) {
		MailchimpContent content = payload.getContentTyped();
		content.getMessage().setTo(mapRecipient(payload.getBody().getRecievingEndpoints().get(0)));
		
		List<MailchimpResponseDto> mailchimpResponseDtos = doSendMessage(MailchimpContentDto.from(content, mailchimpSenderConfig.getAuthKey()));
		payload.getBody().setAttributeValue(MAILCHIMP_RESPONSE_FIELD, mailchimpResponseDtos);
		
		return payload;
	}
	
	public List<MailchimpResponseDto> doSendMessage(MailchimpContentDto contentDto) {
		ResponseEntity<MailchimpResponseDto[]> responseEntity = restTemplate.postForEntity(SEND_TEMPLATE_PATH, contentDto, MailchimpResponseDto[].class);
		
		if (responseEntity.getBody() == null) {
			throw new RestClientException("Response body must not be null");
		}
		
		return Arrays.stream(responseEntity.getBody()).collect(Collectors.toList());
	}
	
	public RestTemplate getRestTemplate() {
		return restTemplate;
	}
	
	private List<MailchimpRecipient> mapRecipient(RecievingEndpoint endpoint) {
		List<MailchimpRecipient> recipientInList = new ArrayList<>();
		
		if (!MailchimpEndpoint.JSON_TYPE_IDENTIFIER.equals(endpoint.getEndpointType())) {
			throw new UnsupportedOperationException("Mapper can only map MailChimpEndpoint endpoint");
		}
		
		MailchimpRecipient recipient = new MailchimpRecipient();
		MailchimpEndpoint mailChimpEndpoint = (MailchimpEndpoint) endpoint;
		recipient.setName(mailChimpEndpoint.getRecipient().getName());
		recipient.setEmail(mailChimpEndpoint.getEmail());
		
		return recipientInList;
	}

}
