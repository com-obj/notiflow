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

package com.obj.nc.functions.sink.inputPersister;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sink.SinkConsumerAdapter;
import com.obj.nc.repositories.GenericEventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class GenericEventPersister extends SinkConsumerAdapter<GenericEvent> {
    
    @Autowired 
    private GenericEventRepository genericEventRepository;
    
	protected Optional<PayloadValidationException> checkPreCondition(GenericEvent payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Could not persist FailedPaylod because its null. Payload: " + payload));
		}
		if (payload.getId()==null) {
			return Optional.of(new PayloadValidationException("Could not persist FailedPaylod because Id is null. Payload: " + payload));
		}		
		if (payload.getFlowId()==null) {
			return Optional.of(new PayloadValidationException("Could not persist FailedPaylod because flowId is null. Payload: " + payload));
		}
		if (payload.getPayloadJson()==null) {
			return Optional.of(new PayloadValidationException("Could not persist FailedPaylod because payload is null. Payload: " + payload));
		}

		return Optional.empty();
	}
	
    protected void execute(GenericEvent payload) {
        log.debug("Persisting generic event {}",payload);
        
        genericEventRepository.save(payload);
    }
    
}
