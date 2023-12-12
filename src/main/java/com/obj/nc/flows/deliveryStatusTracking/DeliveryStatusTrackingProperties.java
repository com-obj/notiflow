package com.obj.nc.flows.deliveryStatusTracking;

import com.obj.nc.domain.dto.fe.DeliveryInfoDto;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static com.obj.nc.flows.deliveryStatusTracking.DeliveryStatusTrackingProperties.CONFIG_PROPS_PREFIX;

@Data
@Configuration
@ConfigurationProperties(prefix = CONFIG_PROPS_PREFIX)
@ToString
public class DeliveryStatusTrackingProperties {
    public static final String CONFIG_PROPS_PREFIX = "nc.flows.delivery-status-tracking";

    private long pollIntervalInSeconds = 600;
    private long maxAgeOfUnfinishedDeliveriesInDays = 30;
    private String[] endpointTypesToTrack = Arrays.stream(DeliveryInfoDto.EndpointType.values()).map(Enum::name).toArray(String[]::new);
}


