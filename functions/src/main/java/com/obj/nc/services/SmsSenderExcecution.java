package com.obj.nc.services;

import java.util.function.Function;

import com.obj.nc.domain.message.Message;

public interface SmsSenderExcecution<RESPONSE_T> extends Function<Message, RESPONSE_T>{

}
