package com.obj.nc.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmitBlogEventDto extends EmitEventDto {

    @NotBlank
    private String id;

    @NotBlank
    private String link;

    @NotEmpty
    private List<String> featuredImage;

    @NotNull
    private BlogCategoryDto category;

    @NotNull
    private BlogAuthorDto author;

}
