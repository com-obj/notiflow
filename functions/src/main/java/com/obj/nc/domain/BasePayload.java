package com.obj.nc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
	@Type(value = NotificationIntent.class, name = NotificationIntent.JSON_TYPE_IDENTIFIER),
	@Type(value = Message.class, name = Message.JSON_TYPE_IDENTIFIER) })
@ToString(callSuper = true)
@Log4j2
public abstract class BasePayload extends BaseJSONObject implements HasHeader, Persistable<UUID> {
	
	@Id
	private UUID id;
	@CreatedDate
	private Instant timeCreated;
	@Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
	protected Header header = new Header();
	@Column("payload_json")
	protected Body body = new Body();
	
	@JsonIgnore
	@Transient
	public ProcessingInfo getProcessingInfo() {
		return getHeader().getProcessingInfo();
	}

	@Transient
	public abstract String getPayloadTypeName();
	
	@JsonIgnore
	@Transient
	public <T> T getContentTyped() {
		return (T)getBody().getMessage();
	}
	
	@Override
	@Transient
	public boolean isNew() {
		return timeCreated == null;
	}

}
