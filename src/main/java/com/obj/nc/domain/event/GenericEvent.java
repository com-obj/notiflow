package com.obj.nc.domain.event;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.event.AfterLoadCallback;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.HasFlowId;
import com.obj.nc.domain.HasJsonPayload;
import com.obj.nc.domain.IsTypedJson;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Table("nc_event")
@Builder
public class GenericEvent implements Persistable<UUID>, HasFlowId, HasJsonPayload, HasHeader, AfterLoadCallback<GenericEvent> {
	

	@Id
	private UUID id;
	//TODO: do not duplicate, use from header
	private String flowId;
	
	private JsonNode payloadJson;

	private String externalId;
	
	@CreatedDate
	private Instant timeCreated;
	//processing started, only if timeConsumed = null processing will be started
	private Instant timeConsumed;
	
	@Transient
	protected Header header;
	
	public static GenericEvent from(JsonNode state) {
		GenericEvent event = new GenericEvent();
		event.setPayloadJson(state);
		event.flowId = state.get("flowId")!=null?state.get("flowId").textValue():"default-flow";
		event.externalId = state.get("externalId")!=null?state.get("externalId").textValue():null;
		
		event.id = UUID.randomUUID();

		event.syncHeaderFields();
		
		return event;
	}
	
	public void syncHeaderFields() {
		getHeader().setFlowId(flowId);
		getHeader().addEventId(id);
	}
	
	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		getHeader().setFlowId(flowId);
		this.flowId = flowId;
	}
	
	public void overrideFlowIdIfApplicable(String flowId) {
    	if (flowId == null) {
    		return;
    	} 
    	
    	this.flowId = flowId;
	}
	
	public void overrideExternalIdIfApplicable(String externalId) {
		if (externalId == null) {
    		return;
    	} 
		
		this.externalId = externalId;
	}

	@Override
	public boolean isNew() {
		return timeCreated == null;
	}
	
	public Header getHeader() {
		if (header==null) {
			header = new Header();
		}
		return header;
	}
	
	public <T extends IsTypedJson> T getPayloadAsPojo() {
		return (T)JsonUtils.readObjectFromJSON(payloadJson, IsTypedJson.class);
	}

	@Override
	//TODO: need to register
	public GenericEvent onAfterLoad(GenericEvent aggregate) {
		aggregate.syncHeaderFields();
		return aggregate;
	}
	
}