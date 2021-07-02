package com.obj.nc.domain.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public class SmsMessage extends Message<SimpleTextContent/*, SmsEndpoint*/> {

	public static final String JSON_TYPE_IDENTIFIER = "SMS_MESSAGE";
	
	public SmsMessage() {
		setBody(new SimpleTextContent());
	}
	
	@Override
	public List<SmsEndpoint> getRecievingEndpoints() {
		return (List<SmsEndpoint>) super.getRecievingEndpoints();
	}

	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
	//TODO: refactor as class parameter
	@JsonIgnore
	public Class<? extends SmsEndpoint> getRecievingEndpointType() {
		return SmsEndpoint.class;
	}
	
}
