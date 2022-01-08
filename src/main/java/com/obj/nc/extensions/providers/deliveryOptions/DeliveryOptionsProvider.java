package com.obj.nc.extensions.providers.deliveryOptions;

import com.obj.nc.domain.deliveryOptions.EndpointDeliveryOptions;
import com.obj.nc.domain.deliveryOptions.RecipientDeliveryOptions;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.recipients.Recipient;

public interface DeliveryOptionsProvider {
    
    public EndpointDeliveryOptions findDeliveryOptions(ReceivingEndpoint forEndpoint);

    public RecipientDeliveryOptions findDeliveryOptions(Recipient forRecipient);

}
