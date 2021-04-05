package com.obj.nc.koderia.functions.processors.senders;

import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.ProcessorMicroService;
import com.obj.nc.koderia.dto.EmitEventDto;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Configuration
@Log4j2
public class MailchimpSenderMicroService extends ProcessorMicroService<Message, Message, MailchimpSenderProcessingFunction> {

	@Autowired
	private MailchimpSenderProcessingFunction fn;

	@Bean
	public Function<Flux<Message>, Flux<Message>> sendMailchimpMessage() {
		return super.executeProccessingService();
	}

	@Override
	public MailchimpSenderProcessingFunction getProccessingFuction() {
		return fn;
	}

}
