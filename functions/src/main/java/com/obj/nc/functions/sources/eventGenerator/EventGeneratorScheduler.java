package com.obj.nc.functions.sources.eventGenerator;


import com.obj.nc.domain.event.Event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(value = "app.scheduling.enabled", havingValue = "true")
@Component
@Profile({"dev", "test", "testmode"})
public class EventGeneratorScheduler {
    @Autowired
    private EventGeneratorMicroService sourceMicroService;

    @Scheduled(fixedDelay = 5000)
    public void generateEventAndAddToFlux() {
        Event event = sourceMicroService.getSourceSupplier().get();
        sourceMicroService.onNext(event);
    }


}