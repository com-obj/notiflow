package com.obj.nc.functions.sink.processingInfoPersister.eventWithRecipients;

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
public class ProcessingInfoPersisterForEventWithRecipientsMicroService
		extends SinkMicroService<BasePayload, ProcessingInfoPersisterForEventWithRecipientsSinkConsumer> {

	@Autowired
	private ProcessingInfoPersisterForEventWithRecipientsSinkConsumer sinkConsumer;

	@Bean
	public Consumer<Flux<BasePayload>> persistPIForEventWithRecipients() {
		return super.executeSinkService();
	}

	@Override
	public ProcessingInfoPersisterForEventWithRecipientsSinkConsumer getSinkConsumer() {
		return sinkConsumer;
	}

}
