package com.obj.nc.controllers;

import java.util.Optional;

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
import com.obj.nc.domain.EventRecieverResponce;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersisterConsumer;
import com.obj.nc.utils.JsonUtils;

import lombok.extern.log4j.Log4j2;

@Validated
@RestController
@RequestMapping("/events")
@Log4j2
public class EventReceiverRestController {
	
	@Autowired
	private GenericEventPersisterConsumer persister;
	
	@PostMapping( consumes="application/json", produces="application/json")
    public EventRecieverResponce emitJobPostEvent(
    		@RequestBody(required = true) String eventJson, 
    		@RequestParam(value = "flowId", required = false) String flowId,
    		@RequestParam(value = "externalId", required = false) String externalId) {
    	
     	JsonNode json = checkIfJsonValidAndReturn(eventJson);
    	
    	GenericEvent event = GenericEvent.from(json);
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

	private JsonNode checkIfJsonValidAndReturn(String eventJson) {
		Optional<String> jsonProblems = JsonUtils.checkValidAndGetError(eventJson);
    	if (jsonProblems.isPresent()) {
    		throw new PayloadValidationException(jsonProblems.get());
    	}
    	
    	return JsonUtils.readJsonNodeFromJSONString(eventJson);
	}



}
