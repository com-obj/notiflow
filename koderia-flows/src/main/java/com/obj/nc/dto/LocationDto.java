package com.obj.nc.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LocationDto {

    @NotBlank
    private String googlePlaceId;

    @NotBlank
    private String name;

}
