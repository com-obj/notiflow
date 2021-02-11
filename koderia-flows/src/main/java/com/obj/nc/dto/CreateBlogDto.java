package com.obj.nc.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateBlogDto extends CreateDto {

    private String id;

    private String link;

    private List<String> featuredImage;

    private BlogCategoryDto category;

    private BlogAuthorDto author;

}
