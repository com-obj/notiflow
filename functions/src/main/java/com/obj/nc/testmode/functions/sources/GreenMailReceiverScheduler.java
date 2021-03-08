package com.obj.nc.testmode.functions.sources;


import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.sources.eventGenerator.EventGeneratorMicroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@ConditionalOnProperty(value = "app.scheduling.enabled", havingValue = "true")
@Component
@Profile({"dev", "test", "testmode"})
public class GreenMailReceiverScheduler {
    @Autowired
    private GreenMailReceiverMicroService sourceMicroService;

    @Scheduled(fixedDelay = 30000)
    public void generateEventAndAddToFlux() {
        List<Message> messages = sourceMicroService.getSourceSupplier().get();
        sourceMicroService.onNext(messages);
    }

}