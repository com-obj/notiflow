package com.obj.nc.functions.processors.senders;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.ProcessorMicroService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Primary
@Configuration
@Log4j2
@AllArgsConstructor
public class EmailSenderReactive extends ProcessorMicroService<Message, Message, EmailSender> {

	private final EmailSender fn;

	@Bean
	public Function<Flux<Message>, Flux<Message>> sendMessage() {
		return super.executeProccessingService();
	}

	@Override
	public EmailSender getProccessingFuction() {
		return fn;
	}

}