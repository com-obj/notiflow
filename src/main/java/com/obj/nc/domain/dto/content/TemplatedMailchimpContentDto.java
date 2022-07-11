package com.obj.nc.domain.dto.content;

import com.obj.nc.domain.Attachment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TemplatedMailchimpContentDto extends BaseMailchimpContentDto {

    private String templateName;
    private Map<String, String> templateContent;
    private String mergeLanguage;
    private Map<String, Object> mergeVariables;

    public static TemplatedMailchimpContentDto create(
            String templateName, Map<String, String> templateContent, String mergeLanguage, Map<String,
            Object> mergeVariables, String subject, List<Attachment> attachments) {
        TemplatedMailchimpContentDto dto = new TemplatedMailchimpContentDto();
        dto.setTemplateName(templateName);
        dto.setTemplateContent(templateContent);
        dto.setMergeLanguage(mergeLanguage);
        dto.setMergeVariables(mergeVariables);
        dto.setSubject(subject);
        dto.setAttachments(attachments);
        return dto;
    }
}
