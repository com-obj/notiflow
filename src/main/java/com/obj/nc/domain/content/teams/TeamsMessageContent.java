package com.obj.nc.domain.content.teams;

import com.obj.nc.domain.content.MessageContent;
import lombok.*;

@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TeamsMessageContent extends MessageContent {
    @NonNull
    private String text;
}
