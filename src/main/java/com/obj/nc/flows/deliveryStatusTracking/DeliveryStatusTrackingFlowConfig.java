package com.obj.nc.flows.deliveryStatusTracking;

import com.obj.nc.domain.dto.fe.DeliveryInfoDto;
import com.obj.nc.functions.sink.deliveryStatusUpdater.ExtensionBasedDeliveryStatusUpdate;
import com.obj.nc.repositories.DeliveryInfoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.messaging.support.GenericMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration(DeliveryStatusTrackingFlowConfig.DELIVERY_STATUS_TRACKING_FLOW_CONF_BEAN_NAME)
@AllArgsConstructor
public class DeliveryStatusTrackingFlowConfig {
    public static final String DELIVERY_STATUS_TRACKING_FLOW_CONF_BEAN_NAME = "DeliveryStatusTrackingFlowConfBean";

    private final ExtensionBasedDeliveryStatusUpdate extensionBasedDeliveryStatusUpdate;
    private final TaskExecutor threadPoolTaskExecutor;

    private final DeliveryStatusTrackingProperties deliveryStatusTrackingProperties;

    @Bean
    public ExecutorChannel executorChannelForDeliveryStatusTracking() {
        return new ExecutorChannel(threadPoolTaskExecutor);
    }

    @Bean
    public IntegrationFlow deliveryStatusTrackerFlow(DeliveryInfoRepository deliveryInfoRepository) {
        return IntegrationFlows.from(
                        () -> {
                            log.info("Polling for deliveries whose status needs an update");
                            Instant now = LocalDateTime.now().toInstant(ZoneOffset.ofTotalSeconds(0));
                            Instant period = now.minus(deliveryStatusTrackingProperties.getMaxAgeOfUnfinishedDeliveriesInDays(), ChronoUnit.DAYS);
                            List<String> endpointTypesToTrack = Arrays.asList(deliveryStatusTrackingProperties.getEndpointTypesToTrack());
                            List<DeliveryInfoDto> deliveries = deliveryInfoRepository.findUnfinishedDeliveriesNotOlderThan(period, endpointTypesToTrack);
                            log.info("Found {} deliveries to update with IDs: {}", deliveries.size(), deliveries.stream().map(DeliveryInfoDto::getDeliveryId).toArray());
                            return new GenericMessage<>(deliveries);
                        },
                        e -> e.poller(Pollers.fixedRate(
                                deliveryStatusTrackingProperties.getPollIntervalInSeconds(), TimeUnit.SECONDS)
                        )
                )
                .split()
                .channel(executorChannelForDeliveryStatusTracking())
                .handle((p) -> {
                    DeliveryInfoDto delivery = (DeliveryInfoDto) p.getPayload();
                    extensionBasedDeliveryStatusUpdate.accept(delivery);
                })
                .get();
    }
}
