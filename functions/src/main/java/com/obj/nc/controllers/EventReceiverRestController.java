package com.obj.nc.controllers;

import com.obj.nc.functions.processors.eventValidator.GenericEventValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.event.EventRecieverResponce;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersisterConsumer;

@Validated
@RestController
@RequestMapping("/events")
public class EventReceiverRestController {

	@Autowired
	private GenericEventPersisterConsumer persister;
	@Autowired
	private GenericEventValidator validator;
	
	@PostMapping( consumes="application/json", produces="application/json")
    public EventRecieverResponce persistGenericEvent(
    		@RequestBody(required = true) String eventJsonString, 
    		@RequestParam(value = "flowId", required = false) String flowId,
    		@RequestParam(value = "externalId", required = false) String externalId) {
    	
     	JsonNode eventJson = validator.apply(eventJsonString);
     	
    	GenericEvent event = GenericEvent.from(eventJson);
    	event.overrideFlowIdIfApplicable(flowId);
    	event.overrideExternalIdIfApplicable(externalId);

    	try {
    		persister.accept(event);
    	} catch (DbActionExecutionException e) {
    		if (DuplicateKeyException.class.equals(e.getCause().getClass())) {
    			throw new PayloadValidationException("Duplicate external ID detected. Payload rejected: " + eventJson);
    		}
    	}

    	return EventRecieverResponce.from(event.getId());
    }

}
