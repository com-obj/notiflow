package com.obj.nc.domain.event;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.HasFlowId;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Table("nc_input")
public class GenericEvent implements Persistable<UUID>, HasFlowId {
	
	@Id
	private UUID id;
	
	private JsonNode payloadJson;
	
	private String flowId;
	private String externalId;
	
	@CreatedDate
	private Instant timeCreated;
	//processing started, only if timeConsumed = null processing will be started
	private Instant timeConsumed;
	
	public static GenericEvent from(JsonNode state) {
		GenericEvent event = new GenericEvent();
		event.setPayloadJson(state);
		event.flowId = state.get("flowId")!=null?state.get("flowId").textValue():null;
		event.externalId = state.get("externalId")!=null?state.get("externalId").textValue():null;
		event.id = UUID.randomUUID();
		return event;
	}

	public void setFlowIdIfNotPresent(String flowId) {
    	if (this.flowId == null) {
    		this.flowId = flowId;
    	} 
    	if (this.flowId == null) {
    		this.flowId = "default-flow";
    	}
	}
	
	public void setExternalIdIfNotPresent(String externalId) {
    	if (this.externalId == null) {
    		this.externalId = externalId;
    	} 
	}

	@Override
	public boolean isNew() {
		return timeCreated == null;
	}

	
}
