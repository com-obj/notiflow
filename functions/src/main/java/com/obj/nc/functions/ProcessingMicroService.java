package com.obj.nc.functions;

import java.util.function.Function;

import reactor.core.publisher.Flux;

public abstract class ProcessingMicroService<IN, OUT, F extends ProcessingFunction<IN,OUT>> {
	
	public Function<Flux<IN>, Flux<OUT>> executeProccessingService() {
		return inputFlux -> inputFlux.map(input -> getProccessingFuction().apply(input));
	}
	
	public abstract F getProccessingFuction();

}
