package com.obj.nc.functions.processors.messageAggregator;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.ProcessorMicroService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;

@Configuration
@Log4j2
public class MessageAggregatorMicroService extends ProcessorMicroService<List<Message>, Message, MessageAggregatorProcessingFunction>{

	@Autowired
	private MessageAggregatorProcessingFunction fn;

	@Bean
	public Function<Flux<List<Message>>, Flux<Message>> aggregateMessages() {
		return super.executeProccessingService();
	}

	@Override
	public MessageAggregatorProcessingFunction getProccessingFuction() {
		return fn;
	}


}
