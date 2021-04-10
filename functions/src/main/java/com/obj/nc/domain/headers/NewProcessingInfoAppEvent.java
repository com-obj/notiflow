package com.obj.nc.domain.headers;

import org.springframework.context.ApplicationEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class NewProcessingInfoAppEvent extends ApplicationEvent {

	private final ProcessingInfo processingInfo;

	public NewProcessingInfoAppEvent(ProcessingInfo processingInfo) {
		super(processingInfo);
		
		this.processingInfo = processingInfo;
	}


}
