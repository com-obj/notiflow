package com.obj.nc.koderia.domain.recipients;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventRecipientsQueryDto extends RecipientsQueryDto {
    
    @NotNull private RecipientsQueryDto.Data data;
    
}
