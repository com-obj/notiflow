package com.obj.nc.extensions.providers.deliveryStatus;

import com.obj.nc.exceptions.PayloadValidationException;

import java.util.Optional;

public interface DeliveryStatusUpdaterExtension<IN> {

        Optional<PayloadValidationException> checkPreCondition(IN payload);

        void updateDeliveryStatus(IN payload);
}
