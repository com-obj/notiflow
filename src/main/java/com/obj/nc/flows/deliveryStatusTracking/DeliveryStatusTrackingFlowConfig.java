package com.obj.nc.flows.deliveryStatusTracking;

import com.obj.nc.extensions.providers.deliveryStatus.DeliveryStatusFetcherExtension;
import com.obj.nc.flows.dataSources.DataSourceFlowsProperties;
import com.obj.nc.flows.dataSources.jdbc.properties.JdbcDataSourceProperties;
import com.obj.nc.flows.dataSources.jdbc.properties.JdbcJobProperties;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryStatusFetcher.ExtensionBasedDeliveryStatusFetcher;
import com.obj.nc.repositories.DeliveryInfoRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.dsl.Pollers;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;

@Configuration(DeliveryStatusTrackingFlowConfig.DELIVERY_STATUS_TRACKING_FLOW_CONF_BEAN_NAME)
@AllArgsConstructor
public class DeliveryStatusTrackingFlowConfig {
    public static final String DELIVERY_STATUS_TRACKING_FLOW_CONF_BEAN_NAME = "DeliveryStatusTrackingFlowConfBean";

    private final ExtensionBasedDeliveryStatusFetcher extensionBasedDeliveryStatusFetcher;
    private final TaskExecutor threadPoolTaskExecutor;

    @Bean
    public ExecutorChannel executorChannelForDeliveryStatusTracking() {
        return new ExecutorChannel(threadPoolTaskExecutor);
    }

    @Bean
    public IntegrationFlow deliveryStatusTrackerFlow(DeliveryInfoRepository deliveryInfoRepository) {
        return IntegrationFlows.from(
                        () -> new GenericMessage<>(
                                deliveryInfoRepository.findPendingDeliveriesNotOlderThan(LocalDateTime.now().toInstant(ZoneOffset.ofTotalSeconds(0)).minus(30, ChronoUnit.DAYS))
                        ),
                        e -> e.poller(Pollers.fixedDelay(1000))
                )
                .split()
                .channel(executorChannelForDeliveryStatusTracking())
                .handle((p) -> {
                    DeliveryInfo delivery = (DeliveryInfo) p.getPayload();
                    extensionBasedDeliveryStatusFetcher.apply(delivery);
                })
                .get();
    }
}
