package com.obj.nc.functions.processors;

import com.obj.nc.domain.event.Event;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Configuration
@Log4j2
public class KoderiaRecipientsMicroService extends ProcessorMicroService<Event, Event, KoderiaRecipientsProcessingFunction>{

	@Autowired
	private KoderiaRecipientsProcessingFunction fn;

	@Bean
	public Function<Flux<Event>, Flux<Event>> resolveKoderiaRecipients() {
		return super.executeProccessingService();
	}

	@Override
	public KoderiaRecipientsProcessingFunction getProccessingFuction() {
		return fn;
	}

}
