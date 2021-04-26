package com.obj.nc.koderia.functions.processors;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.ProcessorMicroService;
import com.obj.nc.koderia.dto.EmitEventDto;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class KoderiaEventConverterMicroService extends ProcessorMicroService<EmitEventDto, NotificationIntent, KoderiaEventConverterProcessingFunction> {

	@Autowired
	private KoderiaEventConverterProcessingFunction fn;

	@Bean
	public Function<Flux<EmitEventDto>, Flux<NotificationIntent>> convertKoderiaNotificationIntent() {
		return super.executeProccessingService();
	}

	@Override
	public KoderiaEventConverterProcessingFunction getProccessingFuction() {
		return fn;
	}

}
