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

import com.obj.nc.domain.dto.MessageTableViewDto;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.domain.message.SendEmailMessageRequest;
import com.obj.nc.domain.message.SendMessageResponse;
import com.obj.nc.flows.messageProcessing.MessageProcessingFlow;
import com.obj.nc.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessagesRestController {
    
    private final MessageProcessingFlow messageProcessingFlow;
    private final MessageRepository messageRepository;
	
	@PostMapping(path = "/send-email", consumes="application/json", produces="application/json")
    public SendMessageResponse receiveEmailMessage(@RequestBody(required = true) SendEmailMessageRequest emailMessageRequest) {
        EmailMessage message = emailMessageRequest.toEmailMessage();
        messageProcessingFlow.processMessage(message);
    	return SendMessageResponse.from(message.getId());
    }
    
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Page<MessageTableViewDto> findAllMessages(@RequestParam(value = "createdFrom", required = false, defaultValue = "2000-01-01T12:00:00Z") 
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
                                                     @RequestParam(value = "createdTo", required = false, defaultValue = "9999-01-01T12:00:00Z") 
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
                                                     @RequestParam(value = "eventId", required = false) String eventId,
                                                     Pageable pageable) {
        UUID eventUUID = eventId == null ? null : UUID.fromString(eventId);
        
        List<MessageTableViewDto> messages = messageRepository
                .findAllMessages(createdFrom, createdTo, eventUUID, pageable.getOffset(), pageable.getPageSize());
        
        long messagesTotalCount = messageRepository.countAllMessages(createdFrom, createdTo, eventUUID);
        return new PageImpl<>(messages, pageable, messagesTotalCount);
    }
    
    @GetMapping(value = "/{messageId}", produces = APPLICATION_JSON_VALUE)
    public MessagePersistentState findMessageById(@PathVariable("messageId") String messageId) {
        return messageRepository
                .findById(UUID.fromString(messageId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    
}
