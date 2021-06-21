package com.obj.nc.controllers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.obj.nc.flows.errorHandling.domain.FailedPaylod;
import com.obj.nc.functions.processors.errorHandling.FailedPaylodExtractor;
import com.obj.nc.repositories.FailedPayloadRepository;

@Validated
@RestController
@RequestMapping("/errors")
public class ErrorHandlingRestController {

	@Autowired private FailedPayloadRepository failedPaylodRepo;
	@Autowired private MessagingTemplate msgTemplate;
	@Autowired private FailedPaylodExtractor extractor;

	
	@PostMapping( consumes="application/json", produces="application/json")
    public void resurrect(
    		@RequestBody(required = true) String failedPaylodId) throws JsonProcessingException {
    	
		Optional<FailedPaylod> oFailedPaylod = failedPaylodRepo.findById(UUID.fromString(failedPaylodId));
		if (!oFailedPaylod.isPresent()) {
			throw new IllegalArgumentException("Failed paylod with " +  failedPaylodId +" ID not found");
		}
		
		FailedPaylod failedPaylod = oFailedPaylod.get();
		if (failedPaylod.getTimeResurected()!= null) {
			throw new IllegalArgumentException("Failed paylod with " +  failedPaylodId +" ID has already been resurected. If it failed again use the newly assigned ID");
		}
		failedPaylod.setTimeResurected(Instant.now());
		failedPaylodRepo.save(failedPaylod);
		
		Message<?> failedMsg = extractor.apply(failedPaylod);
		
		Message<?> msgForRetry = MessageBuilder
				.withPayload(failedMsg.getPayload())
				.copyHeaders(failedMsg.getHeaders())
				.build();
		msgTemplate.send( failedPaylod.getChannelNameForRetry(),  msgForRetry );
    }


}
