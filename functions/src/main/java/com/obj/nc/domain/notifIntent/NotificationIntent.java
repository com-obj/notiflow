package com.obj.nc.domain.notifIntent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.sms.SimpleTextContent;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class NotificationIntent extends BasePayload {
	
	public static final String JSON_TYPE_IDENTIFIER = "EVENT";

	
	public NotificationIntent() {
	}
	
	public static NotificationIntent createWithSimpleMessage(String flowId, String message) {
		NotificationIntent notificationIntent = new NotificationIntent();
		notificationIntent.header.setFlowId(flowId);
		notificationIntent.body.setMessage(new SimpleTextContent(message));
		
		return notificationIntent;
	}

	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
}
