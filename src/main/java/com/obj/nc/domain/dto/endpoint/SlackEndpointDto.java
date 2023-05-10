package com.obj.nc.domain.dto.endpoint;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.endpoints.SlackEndpoint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "channel")
@JsonTypeName("SLACK")
public class SlackEndpointDto extends ReceivingEndpointDto {

    public static final String JSON_TYPE_IDENTIFIER = "SLACK";
    private String channel;

    public static SlackEndpointDto create(String id, String channel) {
        SlackEndpointDto dto = new SlackEndpointDto();
        dto.setId(id);
        dto.setChannel(channel);
        return dto;
    }

    @Override
    public String getValue() {
        return this.channel;
    }

    @Override
    public void setValue(String channel) {
        this.channel = channel;
    }
}
