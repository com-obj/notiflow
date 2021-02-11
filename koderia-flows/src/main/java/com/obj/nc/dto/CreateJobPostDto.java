package com.obj.nc.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateJobPostDto extends CreateDto {

    private String id;

    private String location;

    private String rate;

    private List<String> technologies;

    private String specialRate;

    private List<String> labels;

    private List<String> positionType;

    private String duration;

    private String jobType;

    private String dateOfStart;

}

