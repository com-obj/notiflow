package com.obj.nc.domain.message;

import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
@Table("nc_message")
public abstract class Message<BODY_TYPE extends Content> extends BasePayload<BODY_TYPE> {
	
	@JsonIgnore
	@Transient
	public boolean isEmailMessage() {
		return (this instanceof EmailMessage);
	}
	
	@JsonIgnore
	@Transient
	public boolean isSmsMessage() {
		return (this instanceof SimpleTextMessage);
	}
	
	@JsonIgnore
	@Transient
	public boolean isMailchimpMessage() {
		return (this instanceof MailChimpMessage);
	}


	@JsonIgnore
	@Transient
	public Class<BODY_TYPE> getBodyType() {
		return (Class<BODY_TYPE>)getBody().getClass();
	}
	
	@Transient
	@JsonIgnore
	public abstract Class<? extends RecievingEndpoint> getRecievingEndpointType();
	
}
