package com.obj.nc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BlogCategoryDto {

    private String id;

    private String slug;

    private String name;

    @JsonProperty("color_a")
    private String colorA;

    @JsonProperty("color_b")
    private String colorB;

}
