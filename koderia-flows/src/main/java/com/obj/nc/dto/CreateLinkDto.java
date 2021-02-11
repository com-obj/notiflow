package com.obj.nc.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateLinkDto extends CreateDto {

    private String id;

    private String siteName;

    private List<String> images;

    private String url;

    private List<String> favicons;

}
