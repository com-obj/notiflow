package com.obj.nc.osk.functions.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.osk.functions.model.SalesAgentEventModel;

@JsonTypeName("SALES_AGENT_EMAIL")
public class SalesAgentsEmailTemplate extends TemplateWithModelEmailContent<SalesAgentEventModel> {
    
}
