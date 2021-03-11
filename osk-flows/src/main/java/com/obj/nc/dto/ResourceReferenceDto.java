package com.obj.nc.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ResourceReferenceDto {

    @NotBlank
    private String resourceURL;

}
