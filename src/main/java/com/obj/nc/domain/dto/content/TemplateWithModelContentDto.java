package com.obj.nc.domain.dto.content;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = false)
public class TemplateWithModelContentDto<MODEL_TYPE> extends MessageContentDto {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private MODEL_TYPE model;
    private String templateFileName;
    private List<Locale> requiredLocales = new ArrayList<>();

    public static <MODEL_TYPE> TemplateWithModelContentDto<MODEL_TYPE> create(
            String templateFileName, MODEL_TYPE model, List<Locale> requiredLocales) {
        TemplateWithModelContentDto<MODEL_TYPE> dto = new TemplateWithModelContentDto<>();
        dto.setTemplateFileName(templateFileName);
        dto.setModel(model);
        dto.setRequiredLocales(requiredLocales);
        return dto;
    }
}
