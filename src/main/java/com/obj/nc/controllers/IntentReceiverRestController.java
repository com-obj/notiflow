package com.obj.nc.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.message.MessageReceiverResponse;
import com.obj.nc.domain.notifIntent.IntentReceiverResponse;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.eventValidator.SimpleJsonValidator;
import com.obj.nc.functions.sink.intentPersister.NotificationIntentPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/intents")
public class IntentReceiverRestController {

	@Autowired private NotificationIntentPersister persister;
	@Autowired private SimpleJsonValidator simpleJsonValidator;
	
	@PostMapping(consumes="application/json", produces="application/json")
    public IntentReceiverResponse persistIntent(
    		@RequestBody(required = true) String intentJsonString, 
    		@RequestParam(value = "flowId", required = false) String flowId,
    		@RequestParam(value = "externalId", required = false) String externalId) {
		
		JsonNode intentJson = simpleJsonValidator.apply(intentJsonString);
		NotificationIntent intent = NotificationIntent.from(intentJson);
		
		intent.overrideFlowIdIfApplicable(flowId);
		intent.overrideExternalIdIfApplicable(externalId);

    	try {
    		persister.accept(intent);
    	} catch (DbActionExecutionException e) {
    		if (DuplicateKeyException.class.equals(e.getCause().getClass())) {
    			throw new PayloadValidationException("Duplicate external ID detected. Payload rejected: " + intentJson);
    		}
    	}

    	return IntentReceiverResponse.from(intent.getId());
    }

}
