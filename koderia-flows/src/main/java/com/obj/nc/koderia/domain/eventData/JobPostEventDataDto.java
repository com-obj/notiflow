package com.obj.nc.koderia.domain.eventData;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class JobPostEventDataDto extends BaseKoderiaData {
    
    @EqualsAndHashCode.Include
    @NotBlank private String id;
    @NotBlank private String name;
    @NotBlank private String description;
    @NotBlank private String location;
    @NotBlank private String rate;
    @NotEmpty private List<String> technologies;
    @NotBlank private String specialRate;
    @NotNull private List<String> labels;
    @NotEmpty private List<String> positionType;
    @NotBlank private String duration;
    @NotBlank private String type;
    @NotBlank private String dateOfStart;

}
