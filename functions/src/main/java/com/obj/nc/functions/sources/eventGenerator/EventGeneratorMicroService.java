package com.obj.nc.functions.sources.eventGenerator;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.sources.SourceMicroService;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class EventGeneratorMicroService extends SourceMicroService<NotificationIntent<?>, EventGeneratorSourceSupplier> {

    @Autowired
    private EventGeneratorSourceSupplier supplier;

    @Bean
    public Supplier<Flux<NotificationIntent<?>>> generateNotificationIntent() {
        return super.executeSourceService();
    }

    @Override
    public EventGeneratorSourceSupplier getSourceSupplier() {
        return supplier;
    }

}
