package com.obj.nc.domain.content.mailchimp;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
public class TemplatedMailchimpContent extends BaseMailchimpContent {
    
    private String templateName;
    
    private Map<String, String> templateContent;
    
    private String mergeLanguage;
    
    private Map<String, Object> mergeVariables;
    
}
