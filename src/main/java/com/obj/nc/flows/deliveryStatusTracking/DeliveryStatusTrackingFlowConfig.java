package com.obj.nc.flows.deliveryStatusTracking;

import com.obj.nc.domain.dto.DeliveryInfoDto;
import com.obj.nc.functions.processors.deliveryStatusUpdater.ExtensionBasedDeliveryStatusUpdate;
import com.obj.nc.repositories.DeliveryInfoRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.messaging.support.GenericMessage;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Configuration(DeliveryStatusTrackingFlowConfig.DELIVERY_STATUS_TRACKING_FLOW_CONF_BEAN_NAME)
@AllArgsConstructor
public class DeliveryStatusTrackingFlowConfig {
    public static final String DELIVERY_STATUS_TRACKING_FLOW_CONF_BEAN_NAME = "DeliveryStatusTrackingFlowConfBean";

    private final ExtensionBasedDeliveryStatusUpdate extensionBasedDeliveryStatusUpdate;
    private final TaskExecutor threadPoolTaskExecutor;

    @Bean
    public ExecutorChannel executorChannelForDeliveryStatusTracking() {
        return new ExecutorChannel(threadPoolTaskExecutor);
    }

    @Bean
    public IntegrationFlow deliveryStatusTrackerFlow(DeliveryInfoRepository deliveryInfoRepository) {
        return IntegrationFlows.from(
                        () -> new GenericMessage<>(
                                deliveryInfoRepository.findUnfinishedDeliveriesNotOlderThan(LocalDateTime.now().toInstant(ZoneOffset.ofTotalSeconds(0)).minus(30, ChronoUnit.DAYS))
                        ),
                        e -> e.poller(Pollers.fixedDelay(1000))
                )
                .split()
                .channel(executorChannelForDeliveryStatusTracking())
                .handle((p) -> {
                    DeliveryInfoDto delivery = (DeliveryInfoDto) p.getPayload();
                    extensionBasedDeliveryStatusUpdate.apply(delivery);
                })
                .get();
    }
}
