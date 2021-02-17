package com.obj.nc.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class OrganizerDto {

    @NotNull
    private String description;

    @NotBlank
    private String url;

    @NotBlank
    private String imageUrl;

    @NotBlank
    private String name;

}
