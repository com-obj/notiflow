package com.obj.nc.domain.message;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.Header;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Message extends BasePayload {
	
	private Header header = new Header();
	private Body body = new Body();

	

}
