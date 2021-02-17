package com.obj.nc.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmitLinkEventDto extends EmitEventDto {

    @NotBlank
    private String id;

    @NotBlank
    private String siteName;

    @NotEmpty
    private List<String> images;

    @NotBlank
    private String url;

    @NotNull
    private List<String> favicons;

}
