package com.obj.nc.koderia.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobPostRecipientsQueryDto extends RecipientsQueryDto {

    private List<String> technologies = new ArrayList<>();

    @NotBlank
    private String jobType;

}
