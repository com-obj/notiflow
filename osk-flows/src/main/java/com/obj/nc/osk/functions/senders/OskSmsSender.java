package com.obj.nc.osk.functions.senders;

import org.springframework.stereotype.Component;

import com.obj.nc.functions.processors.senders.BaseSmsSender;
import com.obj.nc.osk.dto.OskSendSmsRequestDto;
import com.obj.nc.osk.dto.OskSendSmsResponseDto;
import com.obj.nc.services.SmsClient;

@Component
public class OskSmsSender extends BaseSmsSender<OskSendSmsRequestDto, OskSendSmsResponseDto> {

    public OskSmsSender(
    		SmsClient<OskSendSmsRequestDto, OskSendSmsResponseDto> smsClient) {
    	
        super(smsClient);
    }

}
