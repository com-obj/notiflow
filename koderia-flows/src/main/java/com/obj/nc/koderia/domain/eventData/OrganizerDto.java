package com.obj.nc.koderia.domain.eventData;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

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
