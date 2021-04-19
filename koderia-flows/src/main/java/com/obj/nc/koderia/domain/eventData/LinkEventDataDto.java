package com.obj.nc.koderia.domain.eventData;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class LinkEventDataDto {

    @NotBlank private String id;
    @NotBlank private String title;
    @NotBlank private String description;
    @NotBlank private String siteName;
    @NotEmpty private List<String> images;
    @NotBlank private String url;
    @NotNull private List<String> favicons;

}
