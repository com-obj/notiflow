package com.obj.nc.functions.processors.messageAggregator;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.ProcessorMicroService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;

import static com.obj.nc.functions.processors.messageAggregator.MessageAggregatorMicroService.SERVICE_NAME;

@Service(SERVICE_NAME)
@Log4j2
public class MessageAggregatorMicroService extends ProcessorMicroService<List<Message>, Message, MessageAggregatorProcessingFunction>
		implements Function<Flux<List<Message>>, Flux<Message>> {

	public static final String SERVICE_NAME = "aggregateMessages";

	@Autowired
	private MessageAggregatorProcessingFunction fn;

	@Override
	public Flux<Message> apply(Flux<List<Message>> messageFlux) {
		return super.executeProccessingService().apply(messageFlux);
	}

	@Override
	public MessageAggregatorProcessingFunction getProccessingFuction() {
		return fn;
	}

}
