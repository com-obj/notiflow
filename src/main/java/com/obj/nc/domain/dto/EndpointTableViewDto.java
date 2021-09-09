package com.obj.nc.domain.dto;

import java.util.UUID;

import com.obj.nc.domain.endpoints.ReceivingEndpoint;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EndpointTableViewDto {
    
    private final UUID uuid;
    private final String name;
    private final EndpointType type;
    private final long sentMessagesCount;
    
    public enum EndpointType {
        EMAIL, SMS, MAILCHIMP, ANY
    }
    
}
