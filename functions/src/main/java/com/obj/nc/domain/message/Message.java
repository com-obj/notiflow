package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.BasePayload;

import com.obj.nc.domain.endpoints.DeliveryOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public class Message extends BasePayload {
	
	public static final String JSON_TYPE_IDENTIFIER = "MESSAGE";

	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}

	@JsonIgnore
	public boolean isAggregateMessage() {
		return this.getBody().getDeliveryOptions().getAggregationType() != DeliveryOptions.AGGREGATION_TYPE.NONE;
	}

	public Message merge(Message other) {
		Message merged = new Message();
		merged.header = header;
		merged.body = body;
		merged.processingInfo = this.processingInfo;

		merged.header = merged.header.merge(other.header);
		merged.body = merged.body.merge(other.body);
		return merged;
	}

}
