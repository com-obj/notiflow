package com.obj.nc.functions.sink.payloadLogger;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.sink.SinkMicroService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Configuration
@Log4j2
public class PaylaodLoggerMicroService extends SinkMicroService<Message, PaylaodLoggerSinkConsumer> {

	@Autowired
	private PaylaodLoggerSinkConsumer fn;

	@Bean
	public Consumer<Flux<Message>> logEvent() {
		return super.executeSinkService();
	}

	@Override
	public PaylaodLoggerSinkConsumer getSinkConsumer() {
		return fn;
	}

}
