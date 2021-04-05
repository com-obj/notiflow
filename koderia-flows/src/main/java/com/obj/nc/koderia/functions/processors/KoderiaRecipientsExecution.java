package com.obj.nc.koderia.functions.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.koderia.dto.RecipientsQueryDto;
import com.obj.nc.koderia.services.KoderiaClient;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.obj.nc.koderia.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;

import java.util.List;
import java.util.function.Function;

@Component
@Log4j2
public class KoderiaRecipientsExecution implements Function<NotificationIntent, NotificationIntent> {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private KoderiaClient koderiaClient;

	@DocumentProcessingInfo("FindKoderiaRecipients")
	@Override
	public NotificationIntent apply(NotificationIntent notificationIntent) {
		Object originalEvent = notificationIntent.getBody().getAttributes().get(ORIGINAL_EVENT_FIELD);
		RecipientsQueryDto recipientsQueryDto = objectMapper.convertValue(originalEvent, RecipientsQueryDto.class);
		List<RecievingEndpoint> emailEndpoints = koderiaClient.findReceivingEndpoints(recipientsQueryDto);
		notificationIntent.getBody().setRecievingEndpoints(emailEndpoints);
		return notificationIntent;
	}

}