package com.obj.nc.osk.functions.sources;

import com.obj.nc.functions.sources.BaseRestReceiverSourceSupplier;
import com.obj.nc.osk.services.OskSmsRestReceiver;
import com.obj.nc.services.BaseRestReceiver;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
public class OskSmsRestReceiverSourceSupplier extends BaseRestReceiverSourceSupplier {
    
    private final OskSmsRestReceiver oskSmsRestReceiver;
    
    @Override
    protected BaseRestReceiver<?, ?> getRestReceiver() {
        return oskSmsRestReceiver;
    }
    
}
