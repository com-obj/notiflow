package com.obj.nc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

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
