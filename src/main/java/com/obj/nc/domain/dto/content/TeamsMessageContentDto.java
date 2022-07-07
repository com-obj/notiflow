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
@JsonTypeName("TEAMS")
public class TeamsMessageContentDto extends MessageContentDto {
    public static final String JSON_TYPE_IDENTIFIER = "TEAMS";
    private String text;

    public static TeamsMessageContentDto create(String text) {
        TeamsMessageContentDto dto = new TeamsMessageContentDto();
        dto.setText(text);
        return dto;
    }
}
