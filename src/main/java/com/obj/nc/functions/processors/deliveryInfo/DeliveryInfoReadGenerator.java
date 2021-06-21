package com.obj.nc.functions.processors.deliveryInfo;

import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
public class DeliveryInfoReadGenerator extends DeliveryInfoSendResultGenerator {
	
	public DeliveryInfoReadGenerator() {
		super(DELIVERY_STATUS.READ);
	}

}
