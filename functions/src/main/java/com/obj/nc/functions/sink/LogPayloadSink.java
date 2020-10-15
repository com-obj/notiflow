package com.obj.nc.functions.sink;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.CreateMessagesFromEvent;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class LogPayloadSink {
		
	@Bean
	public Consumer<Flux<Message>> logEvent() {
		return payloads -> payloads.doOnNext(
				payload -> logEvent(payload)
			).subscribe();
	}
	
	public static void logEvent(Message payload) {
		log.info(payload.toString());
	}

}
