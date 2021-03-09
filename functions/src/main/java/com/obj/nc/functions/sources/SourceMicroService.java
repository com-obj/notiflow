package com.obj.nc.functions.sources;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

public abstract class SourceMicroService<OUT, S extends SourceSupplier<OUT>> {

	private final EmitterProcessor<OUT> streamSource = EmitterProcessor.create();

	protected Supplier<Flux<OUT>> executeSourceService() {
		return () -> streamSource;
	}

	public abstract S getSourceSupplier();

	public void onNext(OUT output) {
		streamSource.onNext(output);
	}

}
