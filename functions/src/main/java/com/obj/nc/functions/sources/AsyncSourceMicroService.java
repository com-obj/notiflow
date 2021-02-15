package com.obj.nc.functions.sources;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

public abstract class AsyncSourceMicroService<OUT> {

	protected final EmitterProcessor<OUT> streamSource = EmitterProcessor.create();

	public Supplier<Flux<OUT>> executeSourceService() {
		return () -> streamSource;
	}

}
