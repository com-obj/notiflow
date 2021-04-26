package com.obj.nc.koderia.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class LocationDto {

    @NotBlank
    private String googlePlaceId;

    @NotBlank
    private String name;

}
