package com.obj.nc.functions.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.Event;
import com.obj.nc.dto.RecipientsQueryDto;
import com.obj.nc.services.KoderiaService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

import static com.obj.nc.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;

@Component
@Log4j2
public class KoderiaRecipientsExecution implements Function<Event, Event> {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private KoderiaService koderiaService;

	@DocumentProcessingInfo("FindKoderiaRecipients")
	@Override
	public Event apply(Event event) {
		Object originalEvent = event.getBody().getAttributes().get(ORIGINAL_EVENT_FIELD);
		RecipientsQueryDto recipientsQueryDto = objectMapper.convertValue(originalEvent, RecipientsQueryDto.class);
		List<RecievingEndpoint> emailEndpoints = koderiaService.findReceivingEndpoints(recipientsQueryDto);
		event.getBody().setRecievingEndpoints(emailEndpoints);
		return event;
	}

}