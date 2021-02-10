package com.obj.nc.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategorizedRecipientsQueryDto extends RecipientsQueryDto {

    private List<String> technologies;

    private String profession;

}
