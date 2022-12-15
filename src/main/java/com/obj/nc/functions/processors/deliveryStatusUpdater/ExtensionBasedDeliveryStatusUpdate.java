package com.obj.nc.functions.processors.deliveryStatusUpdater;

import com.obj.nc.domain.dto.DeliveryInfoDto;
import com.obj.nc.extensions.providers.deliveryStatus.DeliveryStatusUpdaterExtension;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class ExtensionBasedDeliveryStatusUpdate extends ProcessorFunctionAdapter<DeliveryInfoDto, String> {

    private final List<DeliveryStatusUpdaterExtension<DeliveryInfoDto, String>> deliveryStatusUpdaters;

    @Override
    protected String execute(DeliveryInfoDto payload) {
        log.info("Update the status of delivery: {}", payload);
        Optional<DeliveryStatusUpdaterExtension<DeliveryInfoDto, String>> optMatcher = findMatchingDeliveryStatusUpdater(payload);
        optMatcher.map(updater -> updater.updateDeliveryStatus(payload));
        return null;
    }

    private Optional<DeliveryStatusUpdaterExtension<DeliveryInfoDto, String>> findMatchingDeliveryStatusUpdater(DeliveryInfoDto payload) {
        return deliveryStatusUpdaters
                .stream()
                .filter(updater -> !updater.canHandle(payload).isPresent())
                .findAny();
    }
}
