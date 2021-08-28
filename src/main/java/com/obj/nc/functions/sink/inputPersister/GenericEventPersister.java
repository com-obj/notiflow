package com.obj.nc.functions.sink.inputPersister;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sink.SinkConsumerAdapter;
import com.obj.nc.repositories.GenericEventRepository;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
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
