package com.obj.nc.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BasePayload extends BaseJSONObject{

	private ProcessingInfo processingInfo;

	public ProcessingInfo stepStart(String processingStepName) {
		ProcessingInfo processingInfo = new ProcessingInfo();
		processingInfo.stepStart(processingStepName, this);
		this.processingInfo = processingInfo;
		
		return this.processingInfo;
	}
	
	public void stepFinish() {
		this.processingInfo.stepFinish(this);
	}
	


}
