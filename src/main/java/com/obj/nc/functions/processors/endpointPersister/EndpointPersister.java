package com.obj.nc.functions.processors.endpointPersister;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
public class EndpointPersister extends ProcessorFunctionAdapter<BasePayload<?>, BasePayload<?>> {


	@Override
	protected BasePayload<?> execute(BasePayload<?> basePayload) {		
		basePayload.ensureEnpointsPersisted();
		
		return basePayload;
	}



}
