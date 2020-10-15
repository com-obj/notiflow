package com.obj.nc.domain;

import com.obj.nc.domain.event.Event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class MessageContent extends BaseJSONObject {
	
	private String text;
		

}
