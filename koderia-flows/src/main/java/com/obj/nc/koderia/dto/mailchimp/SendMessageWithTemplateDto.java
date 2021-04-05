package com.obj.nc.koderia.dto.mailchimp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SendMessageWithTemplateDto extends SendMessageDto {

    @NotBlank
    @JsonProperty("template_name")
    private String templateName;

    @JsonProperty("template_content")
    private List<TemplateContentDto> templateContent = new ArrayList<>();

}
