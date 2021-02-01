package com.obj.nc.functions;

import java.util.function.Function;

import reactor.core.publisher.Flux;

public abstract class ProcessingMicroService<IN, OUT, COND extends PreCondition<IN>, F extends ProcessingFunction<IN,OUT,COND>> {
	
	public Function<Flux<IN>, Flux<OUT>> executeProccessingService() {
		return inputFlux -> inputFlux.map(input -> getProccessingFuction().apply(input));
	}
	
	public abstract F getProccessingFuction();

}
