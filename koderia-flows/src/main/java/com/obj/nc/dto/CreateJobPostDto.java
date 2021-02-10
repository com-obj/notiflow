package com.obj.nc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

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

