package com.obj.nc.domain.deliveryOptions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class RecipientDeliveryOptions {

    private ChannelSelectionDeliveryOption channelSelectionDO;
}
