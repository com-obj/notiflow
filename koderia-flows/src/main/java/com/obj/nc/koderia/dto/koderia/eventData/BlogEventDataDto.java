package com.obj.nc.koderia.dto.koderia.eventData;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class BlogEventDataDto {
    
    @NotBlank private String id;
    @NotBlank private String title;
    @NotBlank private String content;
    @NotBlank private String link;
    @NotEmpty private List<String> featuredImage;
    @NotNull private BlogCategoryDto category;
    @NotNull private BlogAuthorDto author;
    
}
