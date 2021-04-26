package com.obj.nc.koderia.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobPostEventDataDto extends EventDataDto {

    @NotBlank
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String location;

    @NotBlank
    private String rate;

    @NotEmpty
    private List<String> technologies;

    @NotBlank
    private String specialRate;

    @NotNull
    private List<String> labels;

    @NotEmpty
    private List<String> positionType;

    @NotBlank
    private String duration;

    @NotBlank
    private String type;

    @NotBlank
    private String dateOfStart;

    @Override
    public String getMessageSubject() {
        return name;
    }

    @Override
    public String getMessageText() {
        return description;
    }

}

