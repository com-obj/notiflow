package com.obj.nc.dto.mailchimp;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SendMessageWithTemplateDto extends SendMessageDto {

    @NotBlank
    private String template_name;

    private List<TemplateContentDto> template_content = new ArrayList<>();

}
