package com.obj.nc.domain.message;

import java.beans.Transient;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public class SmsWithTemplatedContent<MODEL_TYPE> extends Message<TemplateWithModelContent<MODEL_TYPE>/*, EmailEndpoint*/> {

	public static final String JSON_TYPE_IDENTIFIER = "SMS_MESSAGE_TEMPLATED_CONTENT";
	
	public SmsWithTemplatedContent() {
		//JSON serialiser
	}
	
	public SmsWithTemplatedContent(TemplateWithModelContent<MODEL_TYPE> content) {
		setBody(content);
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
	@Transient
	@JsonIgnore
	public Class<? extends RecievingEndpoint> getRecievingEndpointType() {
		return null;
	}

}
