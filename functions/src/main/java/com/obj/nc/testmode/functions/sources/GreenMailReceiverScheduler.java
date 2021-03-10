package com.obj.nc.testmode.functions.sources;


import com.obj.nc.domain.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!test")
@EnableScheduling
@ConditionalOnProperty(value = "testmode.enabled", havingValue = "true")
public class GreenMailReceiverScheduler {

    @Autowired
    private GreenMailReceiverMicroService sourceMicroService;

    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    public void generateEventAndAddToFlux() {
        List<Message> messages = sourceMicroService.getSourceSupplier().get();
        sourceMicroService.onNext(messages);
    }

}