package com.obj.nc.domain.message;

import com.obj.nc.domain.content.teams.TeamsMessageContent;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.TeamsEndpoint;

public class TeamsMessage extends Message<TeamsMessageContent> {
    @Override
    public String getPayloadTypeName() {
        return "TEAMS_MESSAGE";
    }

    @Override
    public Class<? extends ReceivingEndpoint> getReceivingEndpointType() {
        return TeamsEndpoint.class;
    }
}
