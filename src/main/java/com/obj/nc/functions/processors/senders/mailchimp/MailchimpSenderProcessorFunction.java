package com.obj.nc.functions.processors.senders.mailchimp;

import static com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfig.MAILCHIMP_RESPONSE_FIELD;
import static com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfig.MAILCHIMP_REST_TEMPLATE;
import static com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfig.SEND_TEMPLATE_PATH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.MailChimpMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.MailchimpSender;
import com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfigProperties;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpRecipientDto;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpSendTemplateRequest;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpSendTemplateResponse;

@DocumentProcessingInfo("SendMailchimpMessage")
public class MailchimpSenderProcessorFunction extends ProcessorFunctionAdapter<MailChimpMessage, MailChimpMessage> implements MailchimpSender {
	
	@Qualifier(MAILCHIMP_REST_TEMPLATE)
	@Autowired private RestTemplate restTemplate;
	@Autowired private MailchimpSenderConfigProperties mailchimpSenderConfigProperties;

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(MailChimpMessage payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Message must not be null"));
		}
		
		MailchimpContent content = payload.getBody();
		if (content == null) {
			return Optional.of(new PayloadValidationException("Message content must not be null"));
		}
		
		boolean hasNoneOrTooMuchEndpoints = payload.getRecievingEndpoints().size() != 1;
		boolean containsNonEmailEndpoint = payload.getRecievingEndpoints().stream()
				.anyMatch(endpoint -> !MailchimpEndpoint.JSON_TYPE_IDENTIFIER.equals(endpoint.getEndpointType()));
		
		if (hasNoneOrTooMuchEndpoints || containsNonEmailEndpoint) {
			return Optional.of(new PayloadValidationException(String.format("MailchimpSender can only send message %s to 1 MailchimpEndpoint", payload)));
		}
		
		return Optional.empty();
	}

	@Override
	protected MailChimpMessage execute(MailChimpMessage payload) {
		MailchimpContent content = payload.getBody();
		content.setRecipients(mapRecipient(payload.getRecievingEndpoints().get(0)));
		
		MailchimpSendTemplateRequest dto = MailchimpSendTemplateRequest.from(content, mailchimpSenderConfigProperties.getAuthKey());
		List<MailchimpSendTemplateResponse> mailchimpSendTemplateResponses = doSendMessage(dto);
		payload.getBody().setAttributeValue(MAILCHIMP_RESPONSE_FIELD, mailchimpSendTemplateResponses);
		
		return payload;
	}
	
	public List<MailchimpSendTemplateResponse> doSendMessage(MailchimpSendTemplateRequest contentDto) {
		ResponseEntity<MailchimpSendTemplateResponse[]> responseEntity = restTemplate.postForEntity(SEND_TEMPLATE_PATH, contentDto, MailchimpSendTemplateResponse[].class);
		
		if (responseEntity.getBody() == null) {
			throw new RestClientException("Response body must not be null");
		}
		
		return Arrays.stream(responseEntity.getBody()).collect(Collectors.toList());
	}
	
	public RestTemplate getRestTemplate() {
		return restTemplate;
	}
	
	public List<MailchimpRecipientDto> mapRecipient(RecievingEndpoint endpoint) {
		List<MailchimpRecipientDto> recipientInList = new ArrayList<>();

		if (!MailchimpEndpoint.JSON_TYPE_IDENTIFIER.equals(endpoint.getEndpointType())) {
			throw new UnsupportedOperationException("Mapper can only map MailChimpEndpoint endpoint");
		}

		MailchimpRecipientDto recipient = new MailchimpRecipientDto();

		MailchimpEndpoint mailChimpEndpoint = (MailchimpEndpoint) endpoint;
		// TODO: uncomment when nc_endpoint table contains recipient data
//		recipient.setName(mailChimpEndpoint.getRecipient().getName());
		recipient.setEmail(mailChimpEndpoint.getEmail());

		recipientInList.add(recipient);
		return recipientInList;
	}

}
