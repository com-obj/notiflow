package com.obj.nc.koderia.functions.processors.recipientsFinder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;

import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.koderia.domain.recipients.RecipientDto;
import com.obj.nc.koderia.domain.recipients.RecipientsQueryDto;
import com.obj.nc.koderia.domain.event.BaseKoderiaEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig.KODERIA_REST_TEMPLATE;
import static com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig.RECIPIENTS_PATH;

@Component
@DocumentProcessingInfo("KoderiaRecipientsFinder")
public class KoderiaRecipientsFinder extends ProcessorFunctionAdapter<NotificationIntent, NotificationIntent> {
	
	@Qualifier(KODERIA_REST_TEMPLATE) 
	@Autowired private RestTemplate restTemplate;
	@Autowired private ObjectMapper objectMapper;
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(NotificationIntent payload) {
		MailchimpContent content = payload.getContentTyped();
		if (content == null) {
			return Optional.of(new PayloadValidationException(String.format("NotificationIntent %s contains null content.", payload)));
		} else if (content.getOriginalEvent() == null) {
			return Optional.of(new PayloadValidationException(String.format("NotificationIntent %s does not contain original event data.", payload)));
		}
		
		MailchimpData dataMergeVar = content.getOriginalEvent();
		
		try {
			objectMapper.convertValue(dataMergeVar, BaseKoderiaEvent.class);
		} catch (IllegalArgumentException e) {
			return Optional.of(new PayloadValidationException(e.getMessage()));
		}
		
		return Optional.empty();
	}
	
	@Override
	protected NotificationIntent execute(NotificationIntent payload) {
		MailchimpData dataMergeVar = payload.<MailchimpContent>getContentTyped().getOriginalEvent();
		RecipientsQueryDto recipientsQueryDto = objectMapper.convertValue(dataMergeVar, RecipientsQueryDto.class);
		
		List<RecievingEndpoint> emailEndpoints = requestReceivingEndpoints(recipientsQueryDto);
		payload.getBody().setRecievingEndpoints(emailEndpoints);
		return payload;
	}
	
	public List<RecievingEndpoint> requestReceivingEndpoints(RecipientsQueryDto query) {
		ResponseEntity<RecipientDto[]> responseEntity = restTemplate.postForEntity(RECIPIENTS_PATH, query, RecipientDto[].class);
		if (responseEntity.getBody() == null) {
			throw new RestClientException("Response body is null");
		}
		return Arrays.stream(responseEntity.getBody()).map(this::mapRecipientToEndpoint).collect(Collectors.toList());
	}
	
	public RestTemplate getRestTemplate() {
		return restTemplate;
	}
	
	public MailchimpEndpoint mapRecipientToEndpoint(RecipientDto recipientDto) {
		if (recipientDto == null) {
			return null;
		}
		
		Person person = new Person(recipientDto.getFirstName() + " " + recipientDto.getLastName());
		MailchimpEndpoint mailChimpEndpoint = MailchimpEndpoint.createForPerson(recipientDto.getEmail(), person);
		DeliveryOptions deliveryOptions = new DeliveryOptions();
		mailChimpEndpoint.setDeliveryOptions(deliveryOptions);
		return mailChimpEndpoint;
	}
}
