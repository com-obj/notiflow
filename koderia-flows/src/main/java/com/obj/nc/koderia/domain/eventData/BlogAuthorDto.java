package com.obj.nc.koderia.domain.eventData;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

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
