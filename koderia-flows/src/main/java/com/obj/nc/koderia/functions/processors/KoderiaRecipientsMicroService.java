package com.obj.nc.koderia.functions.processors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.ProcessorMicroService;

import reactor.core.publisher.Flux;

import java.util.function.Function;

@Configuration
@Log4j2
public class KoderiaRecipientsMicroService extends ProcessorMicroService<NotificationIntent, NotificationIntent, KoderiaRecipientsProcessingFunction>{

	@Autowired
	private KoderiaRecipientsProcessingFunction fn;

	@Bean
	public Function<Flux<NotificationIntent>, Flux<NotificationIntent>> resolveKoderiaRecipients() {
		return super.executeProccessingService();
	}

	@Override
	public KoderiaRecipientsProcessingFunction getProccessingFuction() {
		return fn;
	}

}
