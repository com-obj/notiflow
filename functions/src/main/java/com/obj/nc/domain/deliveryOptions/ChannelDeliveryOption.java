package com.obj.nc.domain.deliveryOptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ChannelDeliveryOption extends BaseDeliveryOption {

	public enum CHANNEL_TYPE {
		EMAIL, SMS, PUSH
	}
	
	private CHANNEL_TYPE channelType = CHANNEL_TYPE.EMAIL;
}
