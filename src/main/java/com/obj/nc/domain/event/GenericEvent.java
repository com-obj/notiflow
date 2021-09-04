package com.obj.nc.domain.event;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.HasEventId;
import com.obj.nc.exceptions.PayloadValidationException;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

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
public class GenericEvent implements Persistable<UUID>, HasFlowId, HasJsonPayload, HasHeader, HasEventId/*, AfterLoadCallback<GenericEvent>*/ {
	
	public static final String DEFUALT_FLOW_ID = "default-flow"; 
	
	@Id
	private UUID id;
	//TODO: do not duplicate, use from header
	@Builder.Default
	private String flowId = DEFUALT_FLOW_ID;
	
	private String payloadType;
	
	private JsonNode payloadJson;

	private String externalId;
	
	@CreatedDate
	private Instant timeCreated;
	//processing started, only if timeConsumed = null processing will be started
	private Instant timeConsumed;
	
	@Transient
	@JsonIgnore
	protected Header header;
	
	public static GenericEvent from(JsonNode state) {
		GenericEvent event = new GenericEvent();
		event.setPayloadJson(state);
		event.flowId = state.get("flowId")!=null?state.get("flowId").textValue():DEFUALT_FLOW_ID;
		event.externalId = state.get("externalId")!=null?state.get("externalId").textValue():null;
		event.payloadType = state.get("payloadType")!=null?state.get("payloadType").textValue():null;
		
		event.id = UUID.randomUUID();

		event.syncHeaderFields();
		
		return event;
	}
	
	public void syncHeaderFields() {
		if (flowId == null) {
			throw new PayloadValidationException("FlowId of GenericEvent must not be null");
		}
		getHeader().setFlowId(flowId);
		if (id == null) {
			throw new PayloadValidationException("Id of GenericEvent must not be null");
		}
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
	
	public void overridePayloadTypeIfApplicable(String payloadType) {
		if (payloadType == null) {
			return;
		}
		
		this.payloadType = payloadType;
	}

	@Override
	@JsonIgnore
	public boolean isNew() {
		return timeCreated == null;
	}
	
	public Header getHeader() {
		if (header==null) {
			header = new Header();
		}
		return header;
	}
	
	@JsonIgnore
	public <T extends IsTypedJson> T getPayloadAsPojo() {
		return (T)JsonUtils.readObjectFromJSON(payloadJson, IsTypedJson.class);
	}
	
	@Override
	@JsonIgnore
	public UUID getEventId() {
		return getId();
	}
	
}
