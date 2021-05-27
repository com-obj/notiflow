package com.obj.nc.osk.functions.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.osk.functions.model.SalesAgentEventModel;

@JsonTypeName("SALES_AGENT_EMAIL")
public class SalesAgentsEmailTemplate extends TemplateWithModelEmailContent<SalesAgentEventModel> {
    
	
	public static final String JSON_TYPE_IDENTIFIER = "SALES_AGENT_EMAIL";

	@Override
	public String getContentTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
}
