package com.obj.nc.domain.dto.content;

import com.obj.nc.domain.Attachment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = false)
public class TemplateWithModelEmailContentDto<MODEL_TYPE> extends TemplateWithModelContentDto<MODEL_TYPE> {

    private String subjectResourceKey;
    private String[] subjectResourcesMessageParameters;
    private String subject;
    private List<Attachment> attachments = new ArrayList<>();

    public static <MODEL_TYPE> TemplateWithModelEmailContentDto<MODEL_TYPE> create(
            String templateFileName, MODEL_TYPE model, List<Locale> requiredLocales, String subjectResourceKey,
            String[] subjectResourcesMessageParameters, String subject, List<Attachment> attachments) {
        TemplateWithModelEmailContentDto<MODEL_TYPE> dto = new TemplateWithModelEmailContentDto<>();
        dto.setTemplateFileName(templateFileName);
        dto.setModel(model);
        dto.setRequiredLocales(requiredLocales);
        dto.setSubjectResourceKey(subjectResourceKey);
        dto.setSubjectResourcesMessageParameters(subjectResourcesMessageParameters);
        dto.setSubject(subject);
        dto.setAttachments(attachments);
        return dto;
    }
}
