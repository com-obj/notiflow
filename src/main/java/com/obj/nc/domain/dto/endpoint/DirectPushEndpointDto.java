package com.obj.nc.domain.dto.endpoint;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "token")
@JsonTypeName("DIRECT_PUSH")
public class DirectPushEndpointDto extends ReceivingEndpointDto {

    public static final String JSON_TYPE_IDENTIFIER = "DIRECT_PUSH";

    private String token;

    public static DirectPushEndpointDto create(String id, String token) {
        DirectPushEndpointDto dto = new DirectPushEndpointDto();
        dto.setId(id);
        dto.setToken(token);
        return dto;
    }

    @Override
    public String getValue() {
        return this.token;
    }

    @Override
    public void setValue(String token) {
        this.token = token;
    }
}
