package com.obj.nc.functions.sink.payloadLogger;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.message.Message;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Log4j2
public class PaylaodLoggerExecution implements Consumer<Message> {

    @DocumentProcessingInfo("LogEvent")
    @Override
    public void accept(Message message) {
        log.info(message.toString());
    }

}