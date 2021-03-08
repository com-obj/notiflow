package com.obj.nc.functions.processors.senders;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.ProcessorMicroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Service(EmailSenderSinkMicroService.SERVICE_NAME)
@Log4j2
@RequiredArgsConstructor
public class EmailSenderSinkMicroService extends ProcessorMicroService<Message, Message, EmailSenderSinkProcessingFunction>
		implements Function<Flux<Message>, Flux<Message>> {

	public static final String SERVICE_NAME = "sendMessage";

	private final EmailSenderSinkProcessingFunction fn;

	@Override
	public Flux<Message> apply(Flux<Message> messageFlux) {
		return super.executeProccessingService().apply(messageFlux);
	}

	@Override
	public EmailSenderSinkProcessingFunction getProccessingFuction() {
		return fn;
	}

}
