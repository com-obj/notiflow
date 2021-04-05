package com.obj.nc.koderia.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class NewsRecipientsQueryDto extends RecipientsQueryDto {
}
