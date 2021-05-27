package com.obj.nc.domain.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
@NoArgsConstructor
public class EmailWithTemplatedContent<BODY_TYPE extends TemplateWithModelEmailContent<?>> extends Message<BODY_TYPE/*, EmailEndpoint*/> {

	public static final String JSON_TYPE_IDENTIFIER = "EMAIL_MESSAGE_TEMPLATED_CONTENT";
	
	public EmailWithTemplatedContent(BODY_TYPE content) {
		setBody(content);
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
	@JsonIgnore
	public Class<? extends RecievingEndpoint> getRecievingEndpointType() {
		return EmailEndpoint.class;
	}

}
