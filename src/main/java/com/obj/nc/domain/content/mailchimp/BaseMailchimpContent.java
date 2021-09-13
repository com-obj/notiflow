package com.obj.nc.domain.content.mailchimp;

import com.obj.nc.domain.Attachment;
import com.obj.nc.domain.content.TrackableContent;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class BaseMailchimpContent extends TrackableContent {
    
    private String subject;
    
    private List<Attachment> attachments = new ArrayList<>();
    
    @Override
    public boolean hasHtmlText() {
        return false;
    }
    
    @Override
    public String getHtmlText() {
        return null;
    }
    
    @Override
    public void setHtmlText(String text) {
        
    }
    
}
