package com.obj.nc.koderia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BlogEventDataDto extends EventDataDto {

    @NotBlank
    private String id;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    private String link;

    @NotEmpty
    private List<String> featuredImage;

    @NotNull
    private BlogCategoryDto category;

    @NotNull
    private BlogAuthorDto author;

    @Override
    public String getMessageSubject() {
        return title;
    }

    @Override
    public String getMessageText() {
        return content;
    }

}