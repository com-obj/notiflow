package com.obj.nc.functions.processors;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.event.Event;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class GenerateProcessingIdProcessor {

//IMERATIVE START
//	@Bean
//	public Function<Event, Event> generateProcessingId() {
//		return GenerateProcessingIdProcessor::generateProcessingId;
//	}
//IMERATIVE END
	
//REACTIVE START
		@Bean
	    public Function<Flux<Event>, Flux<Event>> generateProcessingId() {
			return eventFlux -> eventFlux.map(event -> generateProcessingId(event));
		}
//REACTIVE END

	public Event generateProcessingId(Event event) {
		log.debug("Generating processingID for {}",  event);
		
		event.stepStart("GenerateProcessingIdProcessor");
		
		event.getHeader().generateAndSetID();
		
		event.stepFinish();
		return event;
	}
}
