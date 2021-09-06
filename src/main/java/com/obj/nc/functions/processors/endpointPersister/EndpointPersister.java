package com.obj.nc.functions.processors.endpointPersister;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class EndpointPersister extends ProcessorFunctionAdapter<BasePayload<?>, BasePayload<?>> {


	@Override
	protected BasePayload<?> execute(BasePayload<?> basePayload) {		
		basePayload.ensureEndpointsPersisted();
		
		return basePayload;
	}



}
