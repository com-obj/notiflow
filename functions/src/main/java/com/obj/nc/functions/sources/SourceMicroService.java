package com.obj.nc.functions.sources;

import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

public abstract class SourceMicroService<OUT, S extends SourceSupplier<OUT>> {

	private final EmitterProcessor<OUT> streamSource = EmitterProcessor.create();

	public Supplier<Flux<OUT>> executeSourceService() {
		return () -> streamSource;
	}

	public abstract S getSourceSupplier();

	@Scheduled(fixedDelay = 5000)
	public void generateEventAndAddToFlux() {
		OUT event = getSourceSupplier().get();
		streamSource.onNext(event);
	}

}
