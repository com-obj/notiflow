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
@JsonTypeName("SLACK")
public class SlackMessageContentDto extends MessageContentDto {
    public static final String JSON_TYPE_IDENTIFIER = "SLACK";
    private String text;

    public static SlackMessageContentDto create(String text) {
        SlackMessageContentDto dto = new SlackMessageContentDto();
        dto.setText(text);
        return dto;
    }
}
