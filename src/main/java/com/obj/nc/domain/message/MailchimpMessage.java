package com.obj.nc.domain.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public class MailchimpMessage extends Message<MailchimpContent> {

	public static final String JSON_TYPE_IDENTIFIER = "MAILCHIMP_MESSAGE";
	
	public MailchimpMessage() {
		setBody(new MailchimpContent());
	}

	@Override
	public List<MailchimpEndpoint> getReceivingEndpoints() {
		return (List<MailchimpEndpoint>) super.getReceivingEndpoints();
	}
	
	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
	//TODO: refactor as class parameter
	@JsonIgnore
	public Class<? extends ReceivingEndpoint> getReceivingEndpointType() {
		return MailchimpEndpoint.class;
	}

}
