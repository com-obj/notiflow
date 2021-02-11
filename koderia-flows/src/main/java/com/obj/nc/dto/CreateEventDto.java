package com.obj.nc.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateEventDto extends CreateDto {

    private String id;

    private String ticketUrl;

    private String startAt;

    private String locationId;

    private String endAt;

    private LocationDto location;

    private OrganizerDto organizer;

    private String imageUrl;

}
