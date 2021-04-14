package com.obj.nc.koderia.dto.koderia.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipientDto {

    private String email;

    private String firstName;

    private String lastName;

}
