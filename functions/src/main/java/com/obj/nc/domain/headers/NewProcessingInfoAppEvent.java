package com.obj.nc.domain.headers;

import org.springframework.context.ApplicationEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class NewProcessingInfoAppEvent extends ApplicationEvent {

	private final Header header;

	public NewProcessingInfoAppEvent(Header header) {
		super(header);
		
		this.header = header;
	}


}
