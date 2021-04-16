package com.obj.nc.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.event.EventRecieverResponce;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.services.EventRecieverService;
import com.obj.nc.utils.JsonUtils;

@Validated
@RestController
@RequestMapping("/events")

public class EventReceiverRestController {
	
	@Autowired
	private EventRecieverService delegate;
	
	@PostMapping( consumes="application/json", produces="application/json")
    public EventRecieverResponce emitJobPostEvent(
    		@RequestBody(required = true) String eventJson, 
    		@RequestParam(value = "flowId", required = false) String flowId,
    		@RequestParam(value = "externalId", required = false) String externalId) {
    	
     	JsonNode json = checkIfJsonValidAndReturn(eventJson);

     	return delegate.emitJobPostEvent(json, flowId, externalId);
    }

	private JsonNode checkIfJsonValidAndReturn(String eventJson) {
		Optional<String> jsonProblems = JsonUtils.checkValidAndGetError(eventJson);
    	if (jsonProblems.isPresent()) {
    		throw new PayloadValidationException(jsonProblems.get());
    	}
    	
    	return JsonUtils.readJsonNodeFromJSONString(eventJson);
	}



}
