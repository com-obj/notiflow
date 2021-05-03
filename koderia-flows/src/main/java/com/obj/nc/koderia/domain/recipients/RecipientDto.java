package com.obj.nc.koderia.domain.recipients;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RecipientDto {
    
    @NotBlank private String email;
    @NotNull private String firstName;
    @NotNull private String lastName;

}
