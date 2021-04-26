package com.obj.nc.functions.sources;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.obj.nc.functions.sources.eventGenerator.EventGeneratorExecution;
import com.obj.nc.functions.sources.eventGenerator.EventGeneratorPreCondition;
import com.obj.nc.functions.sources.eventGenerator.EventGeneratorSourceSupplier;

@TestConfiguration
public class EventGeneratorTestConfig {

    @Bean
    public EventGeneratorSourceSupplier generateEvent() {
        return new EventGeneratorSourceSupplier(generateEventExecution(), generateEventPreCondition());
    }

    @Bean
    public EventGeneratorExecution generateEventExecution() {
        return new EventGeneratorExecution();
    }

    @Bean
    public EventGeneratorPreCondition generateEventPreCondition() {
        return new EventGeneratorPreCondition();
    }

}
