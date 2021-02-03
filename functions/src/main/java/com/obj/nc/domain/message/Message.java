package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
		header.merge(other.header);
		body.merge(other.body);
		return this;
	}

	public Message wrapMessageContent(MessageContent wrapperMessageContent) {
		body.getMessage().wrapWith(wrapperMessageContent);
		body.setMessage(wrapperMessageContent);
		return this;
	}
}
