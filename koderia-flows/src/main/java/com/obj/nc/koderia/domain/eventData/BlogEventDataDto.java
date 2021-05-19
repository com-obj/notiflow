package com.obj.nc.koderia.domain.eventData;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class BlogEventDataDto extends BaseKoderiaData {
    
    @EqualsAndHashCode.Include
    @NotBlank private String id;
    @NotBlank private String title;
    @NotBlank private String content;
    @NotBlank private String link;
    @NotEmpty private List<String> featuredImage;
    @NotNull private BlogCategoryDto category;
    @NotNull private BlogAuthorDto author;
    
    @Override
    public Object asMailchimpMergeVarContent() {
        return this;
    }
}
