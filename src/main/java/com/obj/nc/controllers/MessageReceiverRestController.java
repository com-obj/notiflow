package com.obj.nc.controllers;

import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.SendEmailMessageRequest;
import com.obj.nc.domain.message.SendMessageResponse;
import com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlow;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageReceiverRestController {
    private final EmailProcessingFlow emailSendingFlow;
	
	@PostMapping(path = "/send-email", consumes="application/json", produces="application/json")
    public SendMessageResponse receiveEmailMessage(@RequestBody(required = true) SendEmailMessageRequest emailMessageRequest) {
        EmailMessage message = emailMessageRequest.toEmailMessage();
        emailSendingFlow.sendEmail(message);
    	return SendMessageResponse.from(message.getId());
    }
}
