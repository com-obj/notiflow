package com.obj.nc.functions.sink;

import java.util.function.Consumer;

import reactor.core.publisher.Flux;

public abstract class SinkMicroService<IN, C extends SinkConsumer<IN>> {
	
	protected Consumer<Flux<IN>> executeSinkService() {
		return payloads -> payloads.doOnNext(payload -> getSinkConsumer().accept(payload)).subscribe();
	}
	
	public abstract C getSinkConsumer();

}
