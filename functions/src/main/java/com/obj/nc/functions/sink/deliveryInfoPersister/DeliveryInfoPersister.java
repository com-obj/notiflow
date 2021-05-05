package com.obj.nc.functions.sink.deliveryInfoPersister;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.functions.sink.SinkConsumerAdapter;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo;
import com.obj.nc.repositories.DeliveryInfoRepository;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
public class DeliveryInfoPersister extends SinkConsumerAdapter<DeliveryInfo> {

    @Autowired
    private DeliveryInfoRepository deliveryInfoRepo;

	@Override
	protected void execute(DeliveryInfo deliveryInfo) {
		if (deliveryInfo.isNew()) {
			deliveryInfo.setId(UUID.randomUUID());
		}
		
		deliveryInfoRepo.save(deliveryInfo);
	}	

}
