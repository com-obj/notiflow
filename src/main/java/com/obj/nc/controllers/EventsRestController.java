package com.obj.nc.controllers;

import com.obj.nc.functions.processors.eventValidator.GenericEventJsonSchemaValidator;
import com.obj.nc.functions.processors.eventValidator.SimpleJsonValidator;
import com.obj.nc.services.EventsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.event.EventRecieverResponce;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersisterConsumer;

import java.time.Instant;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventsRestController {

	private final GenericEventPersisterConsumer persister;
	private final SimpleJsonValidator simpleJsonValidator;
	private final GenericEventJsonSchemaValidator jsonSchemaValidator;
	private final EventsService eventsService;
	
	@PostMapping( consumes="application/json", produces="application/json")
    public EventRecieverResponce persistGenericEvent(
    		@RequestBody(required = true) String eventJsonString, 
    		@RequestParam(value = "flowId", required = false) String flowId,
    		@RequestParam(value = "externalId", required = false) String externalId,
			@RequestParam(value = "payloadType", required = false) String payloadType) {
		
		JsonNode eventJson = simpleJsonValidator.apply(eventJsonString);
		
		GenericEvent event = GenericEvent.from(eventJson);
    	event.overrideFlowIdIfApplicable(flowId);
    	event.overrideExternalIdIfApplicable(externalId);
    	event.overridePayloadTypeIfApplicable(payloadType);
		
		if (payloadType != null) {
			event = jsonSchemaValidator.apply(event);
		}

    	try {
    		persister.accept(event);
    	} catch (DbActionExecutionException e) {
    		if (DuplicateKeyException.class.equals(e.getCause().getClass())) {
    			throw new PayloadValidationException("Duplicate external ID detected. Payload rejected: " + eventJson);
    		}
    	}

    	return EventRecieverResponce.from(event.getId());
    }
	
	@GetMapping(produces = APPLICATION_JSON_VALUE)
	public Page<GenericEvent> findAllEvents(
			@RequestParam(value = "consumedFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant consumedFrom,
			@RequestParam(value = "consumedTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant consumedTo,
			Pageable pageable) {
		return eventsService.findAllEvents(consumedFrom, consumedTo, pageable);
	}

}
