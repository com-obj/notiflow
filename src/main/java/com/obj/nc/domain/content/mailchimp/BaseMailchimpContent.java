package com.obj.nc.domain.content.mailchimp;

import com.obj.nc.domain.Attachment;
import com.obj.nc.domain.content.MessageContent;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class BaseMailchimpContent extends MessageContent {
    
    private String subject;
    
    private List<Attachment> attachments = new ArrayList<>();
    
}
