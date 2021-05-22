package com.obj.nc.domain.notifIntent;

import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.sms.SimpleTextContent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Table("nc_intent")
public class NotificationIntent<BODY_TYPE extends Content> extends BasePayload<BODY_TYPE> {
	
	public static final String JSON_TYPE_IDENTIFIER = "EVENT";

	public static NotificationIntent<SimpleTextContent> createWithSimpleMessage(String flowId, String message) {
		NotificationIntent<SimpleTextContent> notificationIntent = new NotificationIntent<SimpleTextContent>();
		notificationIntent.header.setFlowId(flowId);
		notificationIntent.setBody(
				SimpleTextContent.builder()
				.text(message)
				.build());
		
		return notificationIntent;
	}

	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}

	
}
