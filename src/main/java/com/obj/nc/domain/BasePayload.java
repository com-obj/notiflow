package com.obj.nc.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.obj.nc.domain.message.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.obj.nc.Get;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.domain.message.EmailWithTestModeDigest;
import com.obj.nc.domain.message.MailchimpMessage;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.domain.message.SmsMessageTemplated;
import com.obj.nc.domain.notifIntent.NotificationIntent;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
	@Type(value = NotificationIntent.class, name = NotificationIntent.JSON_TYPE_IDENTIFIER),
	@Type(value = EmailMessage.class, name = EmailMessage.JSON_TYPE_IDENTIFIER),
	@Type(value = SmsMessage.class, name = SmsMessage.JSON_TYPE_IDENTIFIER),
	@Type(value = MailchimpMessage.class, name = MailchimpMessage.JSON_TYPE_IDENTIFIER),
	@Type(value = MailchimpTemplateMessage.class, name = MailchimpTemplateMessage.JSON_TYPE_IDENTIFIER),
	@Type(value = EmailMessageTemplated.class, name = EmailMessageTemplated.JSON_TYPE_IDENTIFIER),
	@Type(value = EmailWithTestModeDigest.class, name = EmailWithTestModeDigest.JSON_TYPE_IDENTIFIER),
	@Type(value = SmsMessageTemplated.class, name = SmsMessageTemplated.JSON_TYPE_IDENTIFIER),	
})
@ToString(callSuper = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class BasePayload<BODY_TYPE> extends BaseJSONObject implements HasHeader, HasReceivingEndpoints, HasProcessingInfo, Persistable<UUID> {
	
	@Id
	@EqualsAndHashCode.Include
	private UUID id = UUID.randomUUID();
	@CreatedDate
	private Instant timeCreated;
	@Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
	protected Header header = new Header();
	
	@Column("payload_json")	
	protected BODY_TYPE body;
	
	//if body is part of message then receivingEndpoints.size() = 1
	private List<? extends ReceivingEndpoint> receivingEndpoints = new ArrayList<ReceivingEndpoint>();
	
	public BasePayload<BODY_TYPE> addReceivingEndpoints(ReceivingEndpoint ... r) {
		((List<ReceivingEndpoint>)this.receivingEndpoints).addAll(Arrays.asList(r));
		return this;
	}
		
	@JsonIgnore
	@Transient
	public void setReceivingEndpointsSL(List<ReceivingEndpoint> endpoints) {
		List<ReceivingEndpoint> typedEndpoints = Lists.newArrayList(
				   Iterables.filter(endpoints, ReceivingEndpoint.class));
		
		this.setReceivingEndpoints(typedEndpoints);
	}
	
	public void ensureEndpointsPersisted() {		
		List<? extends ReceivingEndpoint> persistedEndpoints = Get.getEndpointsRepo().persistEnpointIfNotExists(getReceivingEndpoints());
		setReceivingEndpoints(persistedEndpoints);
	}
	
	@Override
	@Transient
	public List<? extends ReceivingEndpoint> getReceivingEndpoints() {
		return receivingEndpoints;
	}
	
	@JsonIgnore
	@Transient
	public ProcessingInfo getProcessingInfo() {
		return getHeader().getProcessingInfo();
	}

	@Transient
	public abstract String getPayloadTypeName();
	
	@Override
	@JsonIgnore
	@Transient
	public boolean isNew() {
		return timeCreated == null;
	}

}
