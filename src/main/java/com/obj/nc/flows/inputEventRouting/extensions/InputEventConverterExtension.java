package com.obj.nc.flows.inputEventRouting.extensions;

import java.util.List;
import java.util.Optional;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.IsNotification;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;

/**
 * Will need to allow mapping specific instances of this class to flow_id
 * @author Jan Cuzy
 *
 * @param <RESULT_TYPE>
 */
public interface InputEventConverterExtension<RESULT_TYPE extends IsNotification> {
	
	/**
	 * 
	 * @param payload
	 * @return 
	 * 		Optional.emtpy() if this extensions is capable of making payload->RESULT_TYPE transformation
	 * 		Optional.of(new PayloadValidationException("Error description") if not (error description will be logged)	
	 */
	public Optional<PayloadValidationException> canHandle(GenericEvent payload);

	public List<RESULT_TYPE> convertEvent(GenericEvent event);
}
