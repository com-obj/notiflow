package com.obj.nc.functions.sink;

import reactor.core.publisher.Flux;

import java.util.function.Consumer;

public abstract class SinkMicroService<IN, C extends SinkConsumer<IN>> {
	
	protected Consumer<Flux<IN>> executeSinkService() {
		return payloads -> payloads.doOnNext(payload -> getSinkConsumer().accept(payload)).subscribe();
	}
	
	public abstract C getSinkConsumer();

}
