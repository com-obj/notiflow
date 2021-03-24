package com.obj.nc.services;

import lombok.*;
import lombok.extern.log4j.Log4j2;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Log4j2
public abstract class Sms {

    @NotBlank
    @EqualsAndHashCode.Include
    protected String senderAddress;

    @NotNull
    @EqualsAndHashCode.Include
    protected List<String> address;

    @NotNull
    @EqualsAndHashCode.Include
    protected String message;

}
