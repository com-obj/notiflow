package com.obj.nc.domain.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode
public class SendMessageResponse {
    private UUID ncMessageId;
    
    public static SendMessageResponse from(UUID id) {
        SendMessageResponse resp = new SendMessageResponse();
        resp.ncMessageId = id;
        return resp;
    }
}