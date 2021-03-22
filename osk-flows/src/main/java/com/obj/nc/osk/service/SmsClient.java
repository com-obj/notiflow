package com.obj.nc.osk.service;

import com.obj.nc.osk.dto.SendSmsRequestDto;
import com.obj.nc.osk.dto.SendSmsResponseDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public interface SmsClient {

    SendSmsResponseDto sendSms(@Valid @NotNull SendSmsRequestDto sendSmsRequestDto);

}
