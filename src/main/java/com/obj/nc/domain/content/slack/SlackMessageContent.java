package com.obj.nc.domain.content.slack;

import com.obj.nc.domain.content.MessageContent;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class SlackMessageContent extends MessageContent {
    private String text;
}
