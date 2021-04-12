package com.obj.nc.functions.sources.genericEvents;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;
import com.obj.nc.repositories.GenericEventRepository;

public class GenericEventsForProcessingSupplier extends SourceSupplierAdapter<GenericEvent> {
	
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
		repository.save(eventsToProcess);
		return eventsToProcess;
	}

}
