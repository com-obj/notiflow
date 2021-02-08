package com.obj.nc.domain.koderia;

import lombok.Data;

import java.util.List;

@Data
public class RecipientsQueryDto {

    private String type;

    private List<String> technologies;

    private String profession;

}

