package com.obj.nc.components.api;

import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.MailchimpData;

public interface MailchimpContentFactory {
    
    public MailchimpContent createFromData(MailchimpData event);

}
