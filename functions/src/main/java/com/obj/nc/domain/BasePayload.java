package com.obj.nc.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
	@Type(value = Event.class, name = Event.JSON_TYPE_IDENTIFIER),
	@Type(value = Message.class, name = Message.JSON_TYPE_IDENTIFIER) })
@ToString(callSuper = true)
@Log4j2
public abstract class BasePayload extends BaseJSONObject {

	protected Header header = new Header();
	protected Body body = new Body();

	protected ProcessingInfo processingInfo;

	public ProcessingInfo stepStart(String processingStepName) {
	    log.info("Generating processing info for step {}", processingStepName);
	    
		ProcessingInfo processingInfo = new ProcessingInfo();
		processingInfo.stepStart(processingStepName, this);
		this.processingInfo = processingInfo;

		return this.processingInfo;
	}

	public void stepFinish() {
		this.processingInfo.stepFinish(this);
		
		log.info("Processing finished for step {}. Took {} ms", getProcessingInfo().getStepName(), getProcessingInfo().getDurationInMs());
	}

	public abstract String getPayloadTypeName();

}
