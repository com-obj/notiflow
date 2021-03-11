package com.obj.nc.service;

import com.obj.nc.dto.SendSmsRequestDto;
import com.obj.nc.dto.SendSmsResponseDto;

public interface SmsClient {

    SendSmsResponseDto sendSms(SendSmsRequestDto sendSmsRequestDto);

}
