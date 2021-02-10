package com.obj.nc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateBlogDto extends CreateDto {

    private String id;

    private String link;

    private List<String> featuredImage;

    private BlogCategoryDto category;

    private BlogAuthorDto author;

}
