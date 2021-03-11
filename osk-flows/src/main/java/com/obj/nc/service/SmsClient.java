package com.obj.nc.service;

import com.obj.nc.dto.SendSmsRequestDto;
import com.obj.nc.dto.SendSmsResponseDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public interface SmsClient {

    SendSmsResponseDto sendSms(@Valid @NotNull SendSmsRequestDto sendSmsRequestDto);

}
