package com.obj.nc.koderia.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventEventDataDto extends EventDataDto {

    @NotBlank
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

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

    @Override
    public String getMessageSubject() {
        return name;
    }

    @Override
    public String getMessageText() {
        return description;
    }

}
