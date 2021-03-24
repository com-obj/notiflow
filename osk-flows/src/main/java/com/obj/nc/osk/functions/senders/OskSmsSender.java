package com.obj.nc.osk.functions.senders;

import com.obj.nc.functions.processors.senders.BaseSmsSender;
import com.obj.nc.osk.dto.OskSendSmsRequestDto;
import com.obj.nc.osk.dto.OskSendSmsResponseDto;
import com.obj.nc.services.SmsClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OskSmsSender extends BaseSmsSender<OskSendSmsRequestDto, OskSendSmsResponseDto> {

    private final SmsClient<OskSendSmsRequestDto, OskSendSmsResponseDto> smsClient;

    @Override
    protected SmsClient<OskSendSmsRequestDto, OskSendSmsResponseDto> getSmsClient() {
        return smsClient;
    }

}
