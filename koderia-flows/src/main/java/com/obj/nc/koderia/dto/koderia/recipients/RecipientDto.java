package com.obj.nc.koderia.dto.koderia.recipients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipientDto {
    
    @NotBlank private String email;
    @NotNull private String firstName;
    @NotNull private String lastName;

}
