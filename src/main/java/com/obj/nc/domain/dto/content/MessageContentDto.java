package com.obj.nc.domain.dto.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EmailContentDto.class, name = EmailContentDto.JSON_TYPE_IDENTIFIER),
        @JsonSubTypes.Type(value = SmsContentDto.class, name = SmsContentDto.JSON_TYPE_IDENTIFIER),
})
@Data
public abstract class MessageContentDto {
}
