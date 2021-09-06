package com.obj.nc.domain;

import java.util.List;

import com.obj.nc.domain.endpoints.ReceivingEndpoint;

public interface HasReceivingEndpoints {
	
	List<? extends ReceivingEndpoint> getReceivingEndpoints();

}
