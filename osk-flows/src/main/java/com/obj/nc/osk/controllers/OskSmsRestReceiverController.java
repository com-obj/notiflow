package com.obj.nc.osk.controllers;

import com.obj.nc.osk.dto.OskSendSmsRequestDto;
import com.obj.nc.osk.dto.OskSendSmsResponseDto;
import com.obj.nc.osk.services.OskSmsRestReceiver;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/testmode-receiver")
@AllArgsConstructor
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
public class OskSmsRestReceiverController {
    
    private final OskSmsRestReceiver oskSmsRestReceiver;
    
    @PostMapping(path = "/receive", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OskSendSmsResponseDto receive(OskSendSmsRequestDto request) {
        return oskSmsRestReceiver.receive(request);
    }
    
}
