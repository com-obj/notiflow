package com.obj.nc.controllers;

import java.nio.charset.Charset;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.EventRecieverResponce;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.eventFactory.EventFactoryProcessingFunction;
import com.obj.nc.functions.sink.processingInfoPersister.ProcessingInfoPersisterSinkConsumer;
import com.obj.nc.utils.JsonUtils;

import lombok.extern.log4j.Log4j2;

@Validated
@RestController
@RequestMapping("/events")
@Log4j2
public class EventReceiverRestController {
	
	@Autowired
	private EventFactoryProcessingFunction eventFactory;
	
	@Autowired
	private ProcessingInfoPersisterSinkConsumer persister;
	
	@PostMapping( consumes="application/json", produces="application/json")
    public EventRecieverResponce emitJobPostEvent(
    		@RequestBody String eventJson, 
    		@RequestParam(value = "flowId", required = false) String flowId,
    		@RequestParam(value = "externalId", required = false) String externalId) {
    	
     	JsonNode json = checkIfJsonValidAndReturn(eventJson);
    	
    	GenericEvent event = GenericEvent.from(json);
    	event.setFlowIdIfNotPresent(flowId);

    	Event ncEvent = eventFactory.apply(event);
    	persister.accept(ncEvent);
    	
    	return EventRecieverResponce.from(ncEvent.getHeader().getId());
    }

	private JsonNode checkIfJsonValidAndReturn(String eventJson) {
		Optional<String> jsonProblems = JsonUtils.checkValidAndGetError(eventJson);
    	if (jsonProblems.isPresent()) {
    		throw new PayloadValidationException(jsonProblems.get());
    	}
    	
    	return JsonUtils.readObjectFromJSONString(eventJson);
	}



    @ExceptionHandler({PayloadValidationException.class})
    ResponseEntity<String> handleMethodArgumentNotValidException(PayloadValidationException e) {
        return new ResponseEntity<>("Request not valid becase of invalid payload: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler({MethodArgumentNotValidException.class})
    ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return new ResponseEntity<>("Request arguments not valid: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler({RuntimeException.class})
    ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return new ResponseEntity<>("Unexpected error ocured: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
