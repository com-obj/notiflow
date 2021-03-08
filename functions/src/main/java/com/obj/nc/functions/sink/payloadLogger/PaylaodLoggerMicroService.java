package com.obj.nc.functions.sink.payloadLogger;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.sink.SinkMicroService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Service
@Log4j2
public class PaylaodLoggerMicroService extends SinkMicroService<Message, PaylaodLoggerSinkConsumer>
		implements Consumer<Flux<Message>> {

	@Autowired
	private PaylaodLoggerSinkConsumer fn;

	@Override
	public void accept(Flux<Message> messageFlux) {
		super.executeSinkService().accept(messageFlux);
	}

	@Override
	public PaylaodLoggerSinkConsumer getSinkConsumer() {
		return fn;
	}
}
