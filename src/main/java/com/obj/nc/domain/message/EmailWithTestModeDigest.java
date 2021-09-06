package com.obj.nc.domain.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.flows.testmode.email.functions.processors.TestModeDigestModel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
@NoArgsConstructor
public class EmailWithTestModeDigest extends Message<TemplateWithModelEmailContent<TestModeDigestModel>/*, EmailEndpoint*/> {

	public static final String JSON_TYPE_IDENTIFIER = "EMAIL_MESSAGE_CUSTOM_CONTENT";
	
	public EmailWithTestModeDigest(TemplateWithModelEmailContent<TestModeDigestModel> content) {
		setBody(content);
	}
	
	@Override
	public List<EmailEndpoint> getReceivingEndpoints() {
		return (List<EmailEndpoint>) super.getReceivingEndpoints();
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
