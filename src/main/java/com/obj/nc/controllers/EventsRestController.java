/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.config.PagingConfigProperties;
import com.obj.nc.domain.dto.DeliveryStatsByEndpointType;
import com.obj.nc.domain.dto.GenericEventTableViewDto;
import com.obj.nc.domain.event.EventReceiverResponse;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.pagination.ResultPage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.deliveryStatusTracking.DeliveryStatusTrackingProperties;
import com.obj.nc.flows.eventSummaryNotification.EventSummaryNotificationProperties;
import com.obj.nc.flows.eventSummaryNotification.EventSummaryNotificationProperties.SUMMARY_NOTIF_EVENT_SELECTION;
import com.obj.nc.functions.processors.eventValidator.GenericEventJsonSchemaValidator;
import com.obj.nc.functions.processors.eventValidator.SimpleJsonValidator;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersister;
import com.obj.nc.repositories.GenericEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.obj.nc.utils.PagingUtils.createPageRequest;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Validated
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventsRestController {

	private final GenericEventPersister persister;
	private final SimpleJsonValidator simpleJsonValidator;
	private final GenericEventJsonSchemaValidator jsonSchemaValidator;
	private final GenericEventRepository eventsRepository;
	private final EventSummaryNotificationProperties summaryNotifProps;
	private final PagingConfigProperties pagingConfigProperties;
	private final DeliveryStatusTrackingProperties deliveryStatusTrackingProperties;
	
	@PostMapping( consumes="application/json", produces="application/json")
    public EventReceiverResponse persistGenericEvent(
    		@RequestBody(required = true) String eventJsonString,
    		@RequestParam(value = "flowId", required = false) String flowId,
    		@RequestParam(value = "externalId", required = false) String externalId,
			@RequestParam(value = "payloadType", required = false) String payloadType,
			@RequestParam(value = "notifyAfterProcessing", required = false, defaultValue = "false") boolean notifyAfterProcessing) {
		log.info("Received event with externalId {}", externalId);

		JsonNode eventJson = simpleJsonValidator.apply(eventJsonString);
		
		GenericEvent event = GenericEvent.from(eventJson);
    	event.overrideFlowIdIfApplicable(flowId);
    	event.overrideExternalIdIfApplicable(externalId);
    	event.overridePayloadTypeIfApplicable(payloadType);

		if (summaryNotifProps.getEventSelection() == SUMMARY_NOTIF_EVENT_SELECTION.ALL_EVENT) {
			event.setNotifyAfterProcessing(true);
		} else if (summaryNotifProps.getEventSelection() == SUMMARY_NOTIF_EVENT_SELECTION.SELECTED_EVENTS) {
			event.setNotifyAfterProcessing(notifyAfterProcessing);
		} else {
			event.setNotifyAfterProcessing(false);
		}	
		
		if (payloadType != null) {
			event = jsonSchemaValidator.apply(event);
		}

		log.info("Event with externalId {} validated successfully", externalId);

    	try {
			log.info("Persisting event with externalId {} and id {}", externalId, event.getId());
    		persister.accept(event);
			log.info("Event persisted with externalId {} and id {}", externalId, event.getId());
    	} catch (DbActionExecutionException e) {
			log.error("Error persisting event with externalId {} and id {}", externalId, event.getId(), e);
    		if (DuplicateKeyException.class.equals(e.getCause().getClass())) {
    			throw new PayloadValidationException("Duplicate external ID detected. Payload rejected: " + eventJson);
    		}
    	}
    	return EventReceiverResponse.from(event.getId());
    }
	
	@GetMapping(produces = APPLICATION_JSON_VALUE)
	public Page<GenericEventTableViewDto> findAllEvents(@RequestParam(value = "consumedFrom", required = false, defaultValue = "2000-01-01T12:00:00Z") 
															@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant consumedFrom,
														@RequestParam(value = "consumedTo", required = false, defaultValue = "9999-01-01T12:00:00Z") 
															@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant consumedTo,
														@RequestParam(value = "eventId", required = false) String eventId,
														@RequestParam("page") int page,
														@RequestParam("size") int size) {
		Pageable pageable = createPageRequest(page, size, pagingConfigProperties);
		UUID eventUUID = eventId == null ? null : UUID.fromString(eventId);
		
		List<GenericEventTableViewDto> events = eventsRepository
				.findAllEventsWithStats(consumedFrom, consumedTo, eventUUID, pageable.getOffset(), pageable.getPageSize())
				.stream()
				.map(GenericEventTableViewDto::from)
				.collect(Collectors.toList());
		
		long eventsTotalCount = eventsRepository.countAllEventsWithStats(consumedFrom, consumedTo, eventUUID);
		return new ResultPage<>(events, pageable, eventsTotalCount);
	}

	@GetMapping(value = "/{eventId}/statsByType", produces = APPLICATION_JSON_VALUE)
	public List<DeliveryStatsByEndpointType> findEventStatsByEndpointType(@PathVariable("eventId") String eventId) {
		
		List<DeliveryStatsByEndpointType> events = eventsRepository
				.findEventStatsByEndpointType(UUID.fromString(eventId));
		
		return events;
	}
	

	@GetMapping(value = "/{eventId}", produces = APPLICATION_JSON_VALUE)
	public GenericEvent findEvent(@PathVariable("eventId") String eventId) {
		return eventsRepository
				.findById(UUID.fromString(eventId))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@GetMapping(value = "/summary-notification", produces = APPLICATION_JSON_VALUE)
	public List<GenericEvent> findEventsForSummaryNotification() {
		int secondsSinceLastProcessing = summaryNotifProps.getSecondsSinceLastProcessing();
		Instant now = LocalDateTime.now().toInstant(ZoneOffset.ofTotalSeconds(0));
		Instant before = now.minus(deliveryStatusTrackingProperties.getMaxAgeOfUnfinishedDeliveriesInDays(), ChronoUnit.DAYS);
		List<GenericEvent> events = eventsRepository.findEventsForSummaryNotification(secondsSinceLastProcessing, before);
		return events;
	}
}
