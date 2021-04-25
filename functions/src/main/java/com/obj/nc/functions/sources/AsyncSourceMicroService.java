package com.obj.nc.functions.sources;

import java.util.function.Supplier;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

public abstract class AsyncSourceMicroService<OUT> {

	protected final EmitterProcessor<OUT> streamSource = EmitterProcessor.create();

	public Supplier<Flux<OUT>> executeSourceService() {
		return () -> streamSource;
	}

	public void onNext(OUT output) {
		streamSource.onNext(output);
	}

}
