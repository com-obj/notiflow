package com.obj.nc.dto.mailchimp;

import lombok.Data;

import java.util.List;

@Data
public class RecipientMergeVarsDto {

    private String rcpt;

    private List<MergeVarDto> vars;

}
