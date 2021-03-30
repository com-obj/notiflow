package com.obj.nc.functions.processors.eventIdGenerator;

import java.util.function.Function;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.ProcessorMicroService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class ValidateAndGenerateEventIdMicroService extends ProcessorMicroService<NotificationIntent, NotificationIntent, ValidateAndGenerateEventIdProcessingFunction>{

	@Autowired
	private ValidateAndGenerateEventIdProcessingFunction fn;

	@Bean
	public Function<Flux<NotificationIntent>, Flux<NotificationIntent>> validateAndGenerateNotificationIntentId() {
		return super.executeProccessingService();
	}

	@Override
	public ValidateAndGenerateEventIdProcessingFunction getProccessingFuction() {
		return fn;
	}
	

}
