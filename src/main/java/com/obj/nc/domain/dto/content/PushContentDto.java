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
@JsonTypeName("PUSH")
public class PushContentDto extends MessageContentDto {
    private String subject;
    private String text;
    private String iconUrl;

    public static PushContentDto create(String subject, String text, String iconUrl) {
        PushContentDto dto = new PushContentDto();
        dto.setSubject(subject);
        dto.setText(text);
        dto.setIconUrl(iconUrl);
        return dto;
    }
}
