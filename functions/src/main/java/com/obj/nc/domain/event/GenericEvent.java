package com.obj.nc.domain.event;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class GenericEvent {
	
	private JsonNode state;
	
	private String flowId;
	private String externalId;
	
	public static GenericEvent from(JsonNode state) {
		GenericEvent event = new GenericEvent();
		event.setState(state);
		event.flowId = state.get("flowId")!=null?state.get("flowId").textValue():null;
		event.externalId = state.get("externalId")!=null?state.get("externalId").textValue():null;
		return event;
	}

	public void setFlowIdIfNotPresent(String flowId) {
    	if (this.flowId == null) {
    		this.flowId = flowId;
    	} 
    	if (flowId == null) {
    		flowId = "default-flow";
    	}
	}
	
	public void setExternalIdIfNotPresent(String externalId) {
    	if (this.externalId == null) {
    		this.externalId = externalId;
    	} 
	}
	
}
