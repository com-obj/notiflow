package com.obj.nc.koderia.domain.eventData;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class EventEventDataDto {

    @NotBlank private String id;
    @NotBlank private String name;
    @NotBlank private String description;
    @NotBlank private String ticketUrl;
    @NotBlank private String startAt;
    @NotBlank private String locationId;
    @NotBlank private String endAt;
    @NotNull private LocationDto location;
    @NotNull private OrganizerDto organizer;
    @NotBlank private String imageUrl;

}
