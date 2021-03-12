package com.obj.nc.functions.processors.eventFactory;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.functions.processors.ProcessorMicroService;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class EventFactoryMicroService extends ProcessorMicroService<GenericEvent, Event, EventFactoryProcessingFunction> {

	@Autowired
	private EventFactoryProcessingFunction fn;

	@Bean
	public Function<Flux<GenericEvent>, Flux<Event>> convertKoderiaEvent() {
		return super.executeProccessingService();
	}

	@Override
	public EventFactoryProcessingFunction getProccessingFuction() {
		return fn;
	}

}
