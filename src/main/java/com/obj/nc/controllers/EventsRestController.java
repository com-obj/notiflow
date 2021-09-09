package com.obj.nc.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.event.EventRecieverResponce;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.eventValidator.GenericEventJsonSchemaValidator;
import com.obj.nc.functions.processors.eventValidator.SimpleJsonValidator;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersister;
import com.obj.nc.repositories.GenericEventRepository;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventsRestController {

	private final GenericEventPersister persister;
	private final SimpleJsonValidator simpleJsonValidator;
	private final GenericEventJsonSchemaValidator jsonSchemaValidator;
	private final GenericEventRepository eventsRepository;
	
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
	public Page<GenericEvent> findAllEvents(@RequestParam(value = "consumedFrom", required = false, defaultValue = "2000-01-01T12:00:00Z")
												@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant consumedFrom,
											@RequestParam(value = "consumedTo", required = false, defaultValue = "9999-01-01T12:00:00Z") 
												@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant consumedTo,
											@RequestParam(value = "eventId", required = false) String eventId,
											Pageable pageable) {
		UUID eventUUID = eventId == null ? null : UUID.fromString(eventId);
		List<GenericEvent> events = eventsRepository.findAllByTimeConsumedBetween(consumedFrom, consumedTo, eventUUID, pageable.getOffset(), pageable.getPageSize());
		long eventsTotalCount = eventsRepository.countAllByTimeConsumedBetween(consumedFrom, consumedTo, eventUUID);
		return new PageImpl<>(events, pageable, eventsTotalCount);
	}
	
	@GetMapping(value = "/{eventId}", produces = APPLICATION_JSON_VALUE)
	public GenericEvent findEvent(@PathVariable("eventId") String eventId) {
		return eventsRepository
				.findById(UUID.fromString(eventId))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

}
