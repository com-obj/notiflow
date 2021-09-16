package com.obj.nc.functions.processors.messageTracking;

import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.domain.content.email.EmailContent;
import org.springframework.stereotype.Component;

@Component
public class EmailReadTrackingDecorator extends BaseReadTrackingDecorator<EmailContent> {
    public EmailReadTrackingDecorator(NcAppConfigProperties ncAppConfigProperties) {
        super(ncAppConfigProperties);
    }
}
