package com.obj.nc.functions.processors.eventGenerator;

import java.util.function.Function;

import com.obj.nc.functions.ProcessorMicroService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.event.Event;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class ValidateAndGenerateEventIdMicroService extends ProcessorMicroService<Event, Event, ValidateAndGenerateEventIdProcessingFunction>{

	@Autowired
	private ValidateAndGenerateEventIdProcessingFunction fn;

	@Bean
	public Function<Flux<Event>, Flux<Event>> validateAndGenerateEventId() {
		return super.executeProccessingService();
	}

	@Override
	public ValidateAndGenerateEventIdProcessingFunction getProccessingFuction() {
		return fn;
	}
	

}
