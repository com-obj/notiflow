package com.obj.nc.koderia.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobPostRecipientsQueryDto extends RecipientsQueryDto {

    private List<String> technologies = new ArrayList<>();

    @NotBlank
    private String jobType;

}
