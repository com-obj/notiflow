package com.obj.nc.domain.notifIntent;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.sms.SimpleTextContent;

import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper=false, of = "id")
@Table("nc_intent")
public class NotificationIntent extends BasePayload implements Persistable<UUID> {
	
	public static final String JSON_TYPE_IDENTIFIER = "EVENT";
	
	@Id
	private UUID id;
	@CreatedDate
	private Instant timeCreated;

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
	
	@Override
	@Transient
	public boolean isNew() {
		return timeCreated == null;
	}
}
