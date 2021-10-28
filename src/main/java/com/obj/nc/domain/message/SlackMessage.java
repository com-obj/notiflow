package com.obj.nc.domain.message;

import com.obj.nc.domain.content.slack.SlackMessageContent;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SlackEndpoint;

public class SlackMessage extends Message<SlackMessageContent> {
    @Override
    public String getPayloadTypeName() {
        return "SLACK_MESSAGE";
    }

    @Override
    public Class<? extends ReceivingEndpoint> getReceivingEndpointType() {
        return SlackEndpoint.class;
    }
}
