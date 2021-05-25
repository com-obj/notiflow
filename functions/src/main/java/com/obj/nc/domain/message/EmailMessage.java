package com.obj.nc.domain.message;

import java.beans.Transient;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public class EmailMessage extends Message<EmailContent/*, EmailEndpoint*/> {

	public static final String JSON_TYPE_IDENTIFIER = "EMAIL_MESSAGE";
	
	public EmailMessage() {
		setBody(new EmailContent());
	}
	
	@Override
	public List<EmailEndpoint> getRecievingEndpoints() {
		return (List<EmailEndpoint>) super.getRecievingEndpoints();
	}

	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
	//TODO: refactor as class parameter
	@Transient
	@JsonIgnore
	public Class<? extends RecievingEndpoint> getRecievingEndpointType() {
		return EmailEndpoint.class;
	}

}
