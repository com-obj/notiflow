package com.obj.nc.domain.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public class MailChimpMessage extends Message<MailchimpContent/*, MailchimpEndpoint*/> {

	public static final String JSON_TYPE_IDENTIFIER = "MAILCHIMP_MESSAGE";
	
	public MailChimpMessage() {
		setBody(new MailchimpContent());
	}

	@Override
	public List<MailchimpEndpoint> getRecievingEndpoints() {
		return (List<MailchimpEndpoint>) super.getRecievingEndpoints();
	}
	
	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
	//TODO: refactor as class parameter
	@JsonIgnore
	public Class<? extends RecievingEndpoint> getRecievingEndpointType() {
		return MailchimpEndpoint.class;
	}

}
