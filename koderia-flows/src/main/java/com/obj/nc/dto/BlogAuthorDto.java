package com.obj.nc.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class BlogAuthorDto {

    @NotBlank
    private String id;

    @NotBlank
    private String name;

    @NotNull
    private String description;

    @NotBlank
    private String avatar;

}
