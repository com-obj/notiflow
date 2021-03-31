package com.obj.nc.osk.controllers;

import com.obj.nc.osk.services.sms.OskSmsRestReceiver;
import com.obj.nc.osk.services.sms.dtos.OskSendSmsRequestDto;
import com.obj.nc.osk.services.sms.dtos.OskSendSmsResponseDto;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/API/smsmessaging/v1")
@AllArgsConstructor
public class OskSmsRestReceiverController {
    
    private final OskSmsRestReceiver oskSmsRestReceiver;
    
    @PostMapping(value = "/outbound/{senderAddress}/requests", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OskSendSmsResponseDto receive(@PathVariable String senderAddress, @RequestBody @Valid OskSendSmsRequestDto request) {
        return oskSmsRestReceiver.receive(request);
    }
    
}
