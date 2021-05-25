package com.obj.nc.domain.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.flows.testmode.email.functions.processors.TestModeDiggestMailContent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
@NoArgsConstructor
public class EmailWithTestModeDiggest extends Message<TestModeDiggestMailContent/*, EmailEndpoint*/> {

	public static final String JSON_TYPE_IDENTIFIER = "EMAIL_MESSAGE_CUSTOM_CONTENT";
	
	public EmailWithTestModeDiggest(TestModeDiggestMailContent content) {
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
		return null;
	}

}
