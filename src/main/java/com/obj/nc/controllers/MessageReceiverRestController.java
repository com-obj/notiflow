package com.obj.nc.controllers;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.SendEmailMessageRequest;
import com.obj.nc.domain.message.SendMessageResponse;
import com.obj.nc.flows.messageProcessing.MessageProcessingFlow;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageReceiverRestController {
    private final MessageProcessingFlow messageProcessingFlow;
	
	@PostMapping(path = "/send-email", consumes="application/json", produces="application/json")
    public SendMessageResponse receiveEmailMessage(@RequestBody(required = true) SendEmailMessageRequest emailMessageRequest) {
        EmailMessage message = emailMessageRequest.toEmailMessage();
        messageProcessingFlow.processMessage(message);
    	return SendMessageResponse.from(message.getId());
    }
}
