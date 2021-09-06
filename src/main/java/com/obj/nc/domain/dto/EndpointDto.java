package com.obj.nc.domain.dto;

import java.util.UUID;

import com.obj.nc.domain.endpoints.ReceivingEndpoint;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EndpointDto {
    
    private final UUID uuid;
    private final String name;
    private final EndpointType type;
    private final long sentMessagesCount;
    
    public static EndpointDto from(ReceivingEndpoint receivingEndpoint, long sentMessagesCount) {
        return EndpointDto.builder()
                .uuid(receivingEndpoint.getId())
                .name(receivingEndpoint.getEndpointId())
                .type(EndpointType.valueOf(receivingEndpoint.getEndpointType()))
                .sentMessagesCount(sentMessagesCount)
                .build();
    }
    
    public enum EndpointType {
        EMAIL, SMS, MAILCHIMP, ANY
    }
    
}
