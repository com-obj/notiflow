package com.obj.nc.functions.processors.deliveryInfo;

import org.springframework.stereotype.Component;

import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo.DELIVERY_STATUS;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
public class DeliveryInfoSendGenerator extends DeliveryInfoGenerator {
	
	public DeliveryInfoSendGenerator() {
		super(DELIVERY_STATUS.SEND);
	}

}
