package com.obj.nc.domain.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class MessageContents {

    private MessageContent content = new MessageContent();

    private List<MessageContent> aggregateContent = new ArrayList<>();

    public MessageContents merge(MessageContents other) {
        MessageContents merged = new MessageContents();
        merged.content = content;
        merged.aggregateContent = aggregateContent;
        merged.aggregateContent.addAll(other.aggregateContent);
        return merged;
    }

}
