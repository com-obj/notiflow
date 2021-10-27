package com.obj.nc.domain.endpoints;

import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;

@Builder
@ToString
public class SlackEndpoint extends ReceivingEndpoint {
    public static final String JSON_TYPE_IDENTIFIER = "SLACK";

    @NonNull
    private String channel;

    @Override
    public String getEndpointId() {
        return channel;
    }

    @Override
    public void setEndpointId(String endpointId) {
        channel = endpointId;
    }

    @Override
    public String getEndpointType() {
        return JSON_TYPE_IDENTIFIER;
    }
}
