package com.obj.nc.functions.processors;

import java.util.function.Function;

import reactor.core.publisher.Flux;

public abstract class ProcessorMicroService<IN, OUT, F extends ProcessorFunction<IN,OUT>> {
	
	protected Function<Flux<IN>, Flux<OUT>> executeProccessingService() {
		return inputFlux -> inputFlux.map(input -> getProccessingFuction().apply(input));
	}
	
	public abstract F getProccessingFuction();

}
