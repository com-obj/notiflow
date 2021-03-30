package com.obj.nc.functions.processors.messageBuilder;

import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
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
public class MessagesFromNotificationIntentMicroService extends ProcessorMicroService<NotificationIntent, List<Message>, MessagesFromNotificationIntentProcessingFunction>{

	@Autowired
	private MessagesFromNotificationIntentProcessingFunction fn;

	@Bean
	public Function<Flux<NotificationIntent>, Flux<Message>> generateMessagesFromNotificationIntent() {
		return eventFlux -> super.executeProccessingService().apply(eventFlux).flatMap(Flux::fromIterable);
	}

	@Override
	public MessagesFromNotificationIntentProcessingFunction getProccessingFuction() {
		return fn;
	}

}
