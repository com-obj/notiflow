package com.obj.nc.domain.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@NoArgsConstructor
@ToString(callSuper = false)
public class SmsMessageTemplated<MODEL_TYPE> extends Message<TemplateWithModelContent<MODEL_TYPE>/*, EmailEndpoint*/> {

	public static final String JSON_TYPE_IDENTIFIER = "SMS_MESSAGE_TEMPLATED_CONTENT";
	
	public SmsMessageTemplated(TemplateWithModelContent<MODEL_TYPE> content) {
		setBody(content);
	}
	
	@Override
	public List<SmsEndpoint> getReceivingEndpoints() {
		return (List<SmsEndpoint>) super.getReceivingEndpoints();
	}

	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
	//TODO: refactor as class parameter
	@JsonIgnore
	public Class<? extends ReceivingEndpoint> getReceivingEndpointType() {
		return EmailEndpoint.class;
	}


}
