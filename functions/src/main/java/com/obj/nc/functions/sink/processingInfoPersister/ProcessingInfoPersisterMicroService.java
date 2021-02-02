package com.obj.nc.functions.sink.processingInfoPersister;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.functions.sink.SinkMicroService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Configuration
@Log4j2
public class ProcessingInfoPersisterMicroService extends SinkMicroService<BasePayload, ProcessingInfoPersisterSinkConsumer> {

	@Autowired
	private ProcessingInfoPersisterSinkConsumer sinkConsumer;

	@Bean
	public Consumer<Flux<BasePayload>> persistPIForEvent() {
		return super.executeSinkService();
	}

	@Bean
	public Consumer<Flux<BasePayload>> persistPIForMessage() {
		return super.executeSinkService();
	}

	@Bean
	public Consumer<Flux<BasePayload>> persistPIForSendMessage() {
		return super.executeSinkService();
	}

	@Override
	public ProcessingInfoPersisterSinkConsumer getSinkConsumer() {
		return sinkConsumer;
	}

}
