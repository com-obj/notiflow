package com.obj.nc.dto.mailchimp;

import lombok.Data;

@Data
public class MessageResponseDto {

    private String email;

    private String status;

    private String reject_reason;

    private String _id;

}
