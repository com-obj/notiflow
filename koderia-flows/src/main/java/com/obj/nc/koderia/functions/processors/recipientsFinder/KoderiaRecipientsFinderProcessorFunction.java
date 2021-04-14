package com.obj.nc.koderia.functions.processors.recipientsFinder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;

import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.koderia.dto.EmitEventDto;
import com.obj.nc.koderia.dto.RecipientDto;
import com.obj.nc.koderia.dto.RecipientsQueryDto;
import com.obj.nc.koderia.mapper.RecipientMapper;
import com.obj.nc.koderia.services.KoderiaClient;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.obj.nc.koderia.functions.processors.eventConverter.KoderiaEventConverterProcessorFunction.ORIGINAL_EVENT_FIELD;
import static com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig.KODERIA_REST_TEMPLATE;
import static com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig.RECIPIENTS_PATH;

@Component
@AllArgsConstructor
public class KoderiaRecipientsFinderProcessorFunction extends ProcessorFunctionAdapter<NotificationIntent, NotificationIntent> implements KoderiaClient {
	@Qualifier(KODERIA_REST_TEMPLATE)
	private final RestTemplate restTemplate;
	private final RecipientMapper recipientMapper;
	private final ObjectMapper objectMapper;
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(NotificationIntent payload) {
		boolean eventContainsOriginalEvent = payload.getBody().containsAttributes(Collections.singletonList(ORIGINAL_EVENT_FIELD));
		
		if (!eventContainsOriginalEvent) {
			return Optional.of(new PayloadValidationException(String.format("NotificationIntent %s does not contain required attributes." +
					" Required attributes are: %s", payload.toString(), Collections.singletonList(ORIGINAL_EVENT_FIELD))));
		}
		
		Object originalEvent = payload.getBody().getAttributes().get(ORIGINAL_EVENT_FIELD);
		
		try {
			objectMapper.convertValue(originalEvent, EmitEventDto.class);
		} catch (IllegalArgumentException e) {
			return Optional.of(new PayloadValidationException(e.getMessage()));
		}
		
		return Optional.empty();
	}
	
	@Override
	protected NotificationIntent execute(NotificationIntent payload) {
		Object originalEvent = payload.getBody().getAttributes().get(ORIGINAL_EVENT_FIELD);
		RecipientsQueryDto recipientsQueryDto = objectMapper.convertValue(originalEvent, RecipientsQueryDto.class);
		List<RecievingEndpoint> emailEndpoints = findReceivingEndpoints(recipientsQueryDto);
		payload.getBody().setRecievingEndpoints(emailEndpoints);
		return payload;
	}
	
	@Override
	public List<RecievingEndpoint> findReceivingEndpoints(RecipientsQueryDto query) {
		ResponseEntity<RecipientDto[]> responseEntity = restTemplate.postForEntity(RECIPIENTS_PATH, query, RecipientDto[].class);
		if (responseEntity.getBody() == null) {
			throw new RestClientException("Response body is null");
		}
		return Arrays.stream(responseEntity.getBody()).map(recipientMapper::map).collect(Collectors.toList());
	}
	
	@Override
	public RestTemplate getRestTemplate() {
		return restTemplate;
	}
}
