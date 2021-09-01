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
public abstract class InputEventConverterExtension<RESULT_TYPE extends IsNotification> {
	
	/**
	 * 
	 * @param payload
	 * @return 
	 * 		Optional.emtpy() if this extensions is capable of making payload->RESULT_TYPE transformation
	 * 		Optional.of(new PayloadValidationException("Error description") if not (error description will be logged)	
	 */
	public abstract Optional<PayloadValidationException> canHandle(GenericEvent payload);
	
	public final List<RESULT_TYPE> convertEvent(GenericEvent event) {
		List<RESULT_TYPE> notifications = doConvertEvent(event);
		notifications.forEach(notification -> notification.addEventId(event.getId()));
		return notifications;
	}
	
	public abstract List<RESULT_TYPE> doConvertEvent(GenericEvent event);
	
}
