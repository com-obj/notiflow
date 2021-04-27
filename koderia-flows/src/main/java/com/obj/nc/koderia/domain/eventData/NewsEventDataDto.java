package com.obj.nc.koderia.domain.eventData;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class NewsEventDataDto extends BaseKoderiaData {

    @EqualsAndHashCode.Include
    @NotBlank private String subject;
    
    @EqualsAndHashCode.Include
    @NotBlank private String text;

}
