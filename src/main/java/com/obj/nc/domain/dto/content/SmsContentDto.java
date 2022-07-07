package com.obj.nc.domain.dto.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "text")
@JsonTypeName("SMS")
public class SmsContentDto extends MessageContentDto {

    public static final String JSON_TYPE_IDENTIFIER = "SMS";
    private String text;

    public static SmsContentDto create(String text) {
        SmsContentDto dto = new SmsContentDto();
        dto.setText(text);
        return dto;
    }
}
