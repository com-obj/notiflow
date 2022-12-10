package com.obj.nc.functions.processors.deliveryStatusFetcher;

import com.obj.nc.extensions.providers.deliveryStatus.DeliveryStatusFetcherExtension;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class ExtensionBasedDeliveryStatusFetcher extends ProcessorFunctionAdapter<DeliveryInfo, String> {

    private final List<DeliveryStatusFetcherExtension<DeliveryInfo, String>> deliveryStatusFetchers;

    @Override
    protected String execute(DeliveryInfo payload) {
        // print thread name
        System.out.println("Thread name: " + Thread.currentThread().getName());
        return null;
    }

    private List<DeliveryStatusFetcherExtension<DeliveryInfo, String>> findMatchingDeliveryStatusFetchers(DeliveryInfo payload) {
        return null;
    }
}
