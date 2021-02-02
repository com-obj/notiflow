package com.obj.nc.functions.sources;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class SourceSupplier<OUT> implements Supplier<OUT> {

	@Override
	public OUT get() {
		OUT toGet = execution().get();

		Optional<PayloadValidationException> error = preCondition().apply(toGet);

		if (error.isPresent()) {
			throw error.get();
		}

		return toGet;
	}

	public abstract PreCondition<OUT> preCondition();

	public abstract Supplier<OUT> execution();

}
