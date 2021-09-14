package com.obj.nc.domain.content.mailchimp;

import com.obj.nc.domain.content.TrackableContent;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
public class MailchimpContent extends BaseMailchimpContent implements TrackableContent {
    
    private String html;
    
    @Override
    public boolean hasHtmlText() {
        return getHtml() != null;
    }
    
    @Override
    public String getHtmlText() {
        return getHtml();
    }
    
    @Override
    public void setHtmlText(String text) {
        setHtml(text);
    }
    
}
