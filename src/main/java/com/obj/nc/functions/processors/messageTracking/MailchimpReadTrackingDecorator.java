package com.obj.nc.functions.processors.messageTracking;

import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import org.springframework.stereotype.Component;

@Component
public class MailchimpReadTrackingDecorator extends BaseReadTrackingDecorator<MailchimpContent> {
    public MailchimpReadTrackingDecorator(NcAppConfigProperties ncAppConfigProperties) {
        super(ncAppConfigProperties);
    }
}
