package com.obj.nc.functions.sources.eventGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.notifIntent.NotificationIntent;

@Component
//@Profile({"dev"})
//@EnableScheduling
public class EventGeneratorScheduler {

    @Autowired
    private EventGeneratorMicroService sourceMicroService;

    @Scheduled(fixedDelay = 1000, initialDelay = 1000)
    public void generateEventAndAddToFlux() {
        NotificationIntent notificationIntent = sourceMicroService.getSourceSupplier().get();
        sourceMicroService.onNext(notificationIntent);
    }

}
