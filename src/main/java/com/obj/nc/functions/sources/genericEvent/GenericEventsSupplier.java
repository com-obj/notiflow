package com.obj.nc.functions.sources.genericEvent;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;
import com.obj.nc.repositories.GenericEventRepository;

@DocumentProcessingInfo("InputEventSupplier")
public class GenericEventsSupplier extends SourceSupplierAdapter<GenericEvent> {
	
	@Autowired
	private GenericEventRepository repository;

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(GenericEvent payload) {
		return Optional.empty();
	}

	@Override
	protected GenericEvent execute() {
		GenericEvent eventsToProcess = repository.findFirstByTimeConsumedIsNullOrderByTimeCreatedAsc();

		if (eventsToProcess==null) {
			return null;
		}
		
		eventsToProcess.setTimeConsumed(Instant.now());
		eventsToProcess.syncHeaderFields();
		repository.save(eventsToProcess);
		return eventsToProcess;
	}

}
