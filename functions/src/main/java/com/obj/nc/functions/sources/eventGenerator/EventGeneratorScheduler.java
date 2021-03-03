package com.obj.nc.functions.sources.eventGenerator;

import com.obj.nc.domain.event.Event;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(
        value = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true
)
@Component
public class EventGeneratorScheduler {

    @Autowired
    private EventGeneratorMicroService sourceMicroService;

    @Scheduled(fixedDelay = 1000)
    public void generateEventAndAddToFlux() {
        Event event = sourceMicroService.getSourceSupplier().get();
        sourceMicroService.onNext(event);
    }

}
