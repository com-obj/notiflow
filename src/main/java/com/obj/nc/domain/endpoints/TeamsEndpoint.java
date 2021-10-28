package com.obj.nc.domain.endpoints;

import lombok.*;

@Builder
@ToString
public class TeamsEndpoint extends ReceivingEndpoint {
    public static final String JSON_TYPE_IDENTIFIER = "TEAMS";

    @NonNull
    private String webhookUrl;

    @Override
    public String getEndpointId() {
        return webhookUrl;
    }

    @Override
    public void setEndpointId(String endpointId) {
        webhookUrl = endpointId;
    }

    @Override
    public String getEndpointType() {
        return JSON_TYPE_IDENTIFIER;
    }
}
