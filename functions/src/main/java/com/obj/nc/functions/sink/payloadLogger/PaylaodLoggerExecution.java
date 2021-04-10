package com.obj.nc.functions.sink.payloadLogger;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.message.Message;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@DocumentProcessingInfo("LogEvent")
public class PaylaodLoggerExecution implements Consumer<Message> {


    @Override
    public void accept(Message message) {
        log.info(message.toString());
    }

}