package com.obj.nc.functions.processors.deliveryInfo;

import org.springframework.stereotype.Component;

import com.obj.nc.functions.processors.senders.dtos.DeliveryInfoSendResult;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo.DELIVERY_STATUS;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
public class DeliveryInfoDeliveredGenerator extends DeliveryInfoGenerator {
	
	public DeliveryInfoDeliveredGenerator() {
		super(DELIVERY_STATUS.DELIVERED);
	}
	
	@Override
	protected void adapt(DeliveryInfoSendResult info) {
		super.adapt(info);
	}

}
