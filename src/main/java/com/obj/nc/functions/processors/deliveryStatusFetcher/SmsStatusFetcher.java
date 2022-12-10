package com.obj.nc.functions.processors.deliveryStatusFetcher;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.extensions.providers.deliveryStatus.DeliveryStatusFetcherExtension;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SmsStatusFetcher implements DeliveryStatusFetcherExtension<DeliveryInfo, String> {
    @Override
    public Optional<PayloadValidationException> canHandle(DeliveryInfo payload) {
        return Optional.empty();
    }

    @Override
    public String fetchDeliveryStatus(DeliveryInfo payload) {
        System.out.println("SmsStatusFetcher");
        return null;
    }
}
