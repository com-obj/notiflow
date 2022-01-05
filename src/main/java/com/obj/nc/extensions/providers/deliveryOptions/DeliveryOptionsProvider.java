package com.obj.nc.extensions.providers.deliveryOptions;

import com.obj.nc.domain.deliveryOptions.EndpointDeliveryOptionsConfig;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;

public interface DeliveryOptionsProvider {
    
    public EndpointDeliveryOptionsConfig findDeliveryOptions(ReceivingEndpoint forEndpoint);

}
