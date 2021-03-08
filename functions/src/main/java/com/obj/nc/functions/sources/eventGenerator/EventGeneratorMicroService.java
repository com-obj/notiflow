package com.obj.nc.functions.sources.eventGenerator;

import com.obj.nc.domain.event.Event;
import com.obj.nc.functions.sources.SourceMicroService;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

@Configuration
@Log4j2
@Profile({"dev", "testmode"})
public class EventGeneratorMicroService extends SourceMicroService<Event, EventGeneratorSourceSupplier> {

    @Autowired
    private EventGeneratorSourceSupplier supplier;

    @Bean
    public Supplier<Flux<Event>> generateEvent() {
        return super.executeSourceService();
    }

    @Override
    public EventGeneratorSourceSupplier getSourceSupplier() {
        return supplier;
    }

}
