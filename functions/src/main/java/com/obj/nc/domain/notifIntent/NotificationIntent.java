package com.obj.nc.domain.notifIntent;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.sms.SimpleTextContent;

import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Table("nc_intent")
public class NotificationIntent extends BasePayload {
	
	public static final String JSON_TYPE_IDENTIFIER = "EVENT";

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
