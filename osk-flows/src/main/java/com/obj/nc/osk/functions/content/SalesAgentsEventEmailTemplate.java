package com.obj.nc.osk.functions.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.TemplateWithModelBasedContent;
import com.obj.nc.osk.functions.model.SalesAgentEventModel;

@JsonTypeName("SALES_AGENT_EVENT")
public class SalesAgentsEventEmailTemplate extends TemplateWithModelBasedContent<SalesAgentEventModel> {
    
}
