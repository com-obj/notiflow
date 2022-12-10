package com.obj.nc.extensions.providers.deliveryStatus;

import com.obj.nc.exceptions.PayloadValidationException;

import java.util.Optional;

public interface DeliveryStatusFetcherExtension<IN, OUT> {

        Optional<PayloadValidationException> canHandle(IN payload);

        OUT fetchDeliveryStatus(IN payload);
}
