package com.obj.nc.osk.functions.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.message.BaseEmailFromTemplate;
import com.obj.nc.osk.functions.model.SalesAgentEventStartModel;

@JsonTypeName("SALES_AGENT_EVENT_START")
public class SalesAgentsEventStartEmailTemplate extends BaseEmailFromTemplate<SalesAgentEventStartModel> {

    @Override
    public String getContentTypeName() {
        return "SALES_AGENT_EVENT_START";
    }

}
