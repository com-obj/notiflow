package com.obj.nc.functions.sink.deliveryStatusUpdater;

import com.obj.nc.domain.dto.fe.DeliveryInfoDto;
import com.obj.nc.extensions.providers.deliveryStatus.DeliveryStatusUpdaterExtension;
import com.obj.nc.functions.sink.SinkConsumerAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class ExtensionBasedDeliveryStatusUpdate extends SinkConsumerAdapter<DeliveryInfoDto> {

    private final List<DeliveryStatusUpdaterExtension<DeliveryInfoDto>> deliveryStatusUpdaters;

    @Override
    protected void execute(DeliveryInfoDto payload) {
        log.info("Update the status of delivery: {}", payload);
        Optional<DeliveryStatusUpdaterExtension<DeliveryInfoDto>> optMatcher = findMatchingDeliveryStatusUpdater(payload);
        optMatcher.ifPresent(updater -> updater.updateDeliveryStatus(payload));
    }

    private Optional<DeliveryStatusUpdaterExtension<DeliveryInfoDto>> findMatchingDeliveryStatusUpdater(DeliveryInfoDto payload) {
        return deliveryStatusUpdaters
                .stream()
                .filter(updater -> !updater.checkPreCondition(payload).isPresent())
                .findAny();
    }
}
