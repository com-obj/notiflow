package com.obj.nc.osk.functions.sources;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.obj.nc.functions.sources.BaseRestReceiverSourceSupplier;
import com.obj.nc.osk.services.sms.dtos.OskSendSmsRequestDto;
import com.obj.nc.services.BaseTestModeSmsReceiver;

@Component
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
public class OskSmsRestReceiverSourceSupplier extends BaseRestReceiverSourceSupplier<OskSendSmsRequestDto> {
    
    public OskSmsRestReceiverSourceSupplier(
    		BaseTestModeSmsReceiver<OskSendSmsRequestDto, ?> restReceiver) {
    	
        super(restReceiver);
    }
    
}
