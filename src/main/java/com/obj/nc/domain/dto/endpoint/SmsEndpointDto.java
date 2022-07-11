package com.obj.nc.domain.dto.endpoint;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "phone")
@JsonTypeName("SMS")
public class SmsEndpointDto extends ReceivingEndpointDto {

    public static final String JSON_TYPE_IDENTIFIER = "SMS";
    private String phone;

    public static SmsEndpointDto create(String id, String phone) {
        SmsEndpointDto dto = new SmsEndpointDto();
        dto.setId(id);
        dto.setPhone(phone);
        return dto;
    }

    @Override
    public String getValue() {
        return this.phone;
    }

    @Override
    public void setValue(String phone) {
        this.phone = phone;
    }
}
