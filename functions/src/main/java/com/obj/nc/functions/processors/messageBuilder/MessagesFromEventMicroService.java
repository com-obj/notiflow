package com.obj.nc.functions.processors.messageBuilder;

import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.ProcessorMicroService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;

@Configuration
@Log4j2
public class MessagesFromEventMicroService extends ProcessorMicroService<Event, List<Message>, MessagesFromEventProcessingFunction>{

	@Autowired
	private MessagesFromEventProcessingFunction fn;

	@Bean
	public Function<Flux<Event>, Flux<Message>> generateMessagesFromEvent() {
		return eventFlux -> super.executeProccessingService().apply(eventFlux).flatMap(Flux::fromIterable);
	}

	@Override
	public MessagesFromEventProcessingFunction getProccessingFuction() {
		return fn;
	}

}
