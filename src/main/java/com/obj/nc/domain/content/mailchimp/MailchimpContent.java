package com.obj.nc.domain.content.mailchimp;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
public class MailchimpContent extends BaseMailchimpContent {
    
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
