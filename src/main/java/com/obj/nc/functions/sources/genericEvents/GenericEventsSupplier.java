/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.functions.sources.genericEvents;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;
import com.obj.nc.repositories.GenericEventRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;

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
		//this is duplicating eventIds
//		eventsToProcess.syncHeaderFields();
		repository.save(eventsToProcess);
		return eventsToProcess;
	}

}
