package com.obj.nc.domain.content.intent;

import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.deliveryOptions.ChannelDeliveryOption;

public abstract class BaseIntentContent extends Content  {

	public abstract Content createMessageContent(ChannelDeliveryOption.CHANNEL_TYPE type);

}
