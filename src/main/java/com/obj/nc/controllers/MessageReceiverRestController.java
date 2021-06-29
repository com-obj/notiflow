package com.obj.nc.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessageReceiverResponse;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.eventValidator.SimpleJsonValidator;
import com.obj.nc.functions.sink.inputPersister.MessagePersisterConsumer;
import com.obj.nc.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/events")
public class MessageReceiverRestController {

	@Autowired
	private MessagePersisterConsumer persister;
	@Autowired
	private SimpleJsonValidator simpleJsonValidator;
	
	@PostMapping( consumes="application/json", produces="application/json")
    public MessageReceiverResponse persistMessage(
    		@RequestBody(required = true) String messageJsonString, 
    		@RequestParam(value = "flowId", required = false) String flowId,
    		@RequestParam(value = "externalId", required = false) String externalId) {
		
		JsonNode messageJson = simpleJsonValidator.apply(messageJsonString);
		Message<?> message = JsonUtils.readObjectFromJSON(messageJson, Message.class);
		
		message.setFlowIdOrDefault(flowId);
		message.overrideExternalIdIfApplicable(externalId);

    	try {
    		persister.accept(message);
    	} catch (DbActionExecutionException e) {
    		if (DuplicateKeyException.class.equals(e.getCause().getClass())) {
    			throw new PayloadValidationException("Duplicate external ID detected. Payload rejected: " + messageJson);
    		}
    	}

    	return MessageReceiverResponse.from(message.getId());
    }

}
