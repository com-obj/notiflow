package com.obj.nc.controllers;

import com.obj.nc.domain.dto.MessageTableViewDto;
import com.obj.nc.domain.message.*;
import com.obj.nc.repositories.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.obj.nc.flows.messageProcessing.MessageProcessingFlow;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
                                                     Pageable pageable) {
        List<MessageTableViewDto> messages = messageRepository
                .findAllByTimeCreatedBetween(createdFrom, createdTo, pageable)
                .stream()
                .map(MessageTableViewDto::from)
                .collect(Collectors.toList());
        
        long messagesTotalCount = messageRepository.countAllByTimeCreatedBetween(createdFrom, createdTo);
        return new PageImpl<>(messages, pageable, messagesTotalCount);
    }
    
    @GetMapping(value = "/{messageId}", produces = APPLICATION_JSON_VALUE)
    public MessagePersistentState findMessageById(@PathVariable("messageId") String messageId) {
        return messageRepository
                .findById(UUID.fromString(messageId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    
}
