package com.obj.nc.domain.dto;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EndpointDto {
    
    private final UUID uuid;
    private final String name;
    private final EndpointType type;
    private final long sentMessages;
    
    public static EndpointDto from(RecievingEndpoint receivingEndpoint, long sentMessages) {
        return EndpointDto.builder()
                .uuid(receivingEndpoint.getId())
                .name(receivingEndpoint.getEndpointId())
                .type(EndpointType.valueOf(receivingEndpoint.getEndpointType()))
                .sentMessages(sentMessages)
                .build();
    }
    
    public enum EndpointType {
        EMAIL, SMS, MAILCHIMP
    }
    
}
