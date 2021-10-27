package com.obj.nc.functions.processors.senders.slack;

import lombok.Data;

@Data
public class SlackResponse {
    private boolean ok;
    private String error;
}
