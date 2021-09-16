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
import com.obj.nc.flows.errorHandling.domain.FailedPayload;
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
    	
		Optional<FailedPayload> oFailedPaylod = failedPaylodRepo.findById(UUID.fromString(failedPaylodId));
		if (!oFailedPaylod.isPresent()) {
			throw new IllegalArgumentException("Failed paylod with " +  failedPaylodId +" ID not found");
		}
		
		FailedPayload failedPaylod = oFailedPaylod.get();
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
