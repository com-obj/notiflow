package com.obj.nc.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmitEventEventDto extends EmitEventDto {

    @NotBlank
    private String id;

    @NotBlank
    private String ticketUrl;

    @NotBlank
    private String startAt;

    @NotBlank
    private String locationId;

    @NotBlank
    private String endAt;

    @NotNull
    private LocationDto location;

    @NotNull
    private OrganizerDto organizer;

    @NotBlank
    private String imageUrl;

}
