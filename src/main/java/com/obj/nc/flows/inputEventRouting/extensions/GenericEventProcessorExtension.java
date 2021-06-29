package com.obj.nc.flows.inputEventRouting.extensions;

import java.util.List;
import java.util.Optional;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;

/**
 * Will need to allow mapping specific instances of this class to flow_id
 * @author Jan Cuzy
 *
 * @param <RESULT_TYPE>
 */
public interface GenericEventProcessorExtension<RESULT_TYPE extends BasePayload<?>> {
	
	public Optional<PayloadValidationException> checkPreCondition(GenericEvent payload);

	public List<RESULT_TYPE> convertEvent(GenericEvent event);
}
