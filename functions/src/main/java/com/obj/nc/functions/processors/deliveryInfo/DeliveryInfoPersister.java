package com.obj.nc.functions.processors.deliveryInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.DeliveryInfoRepository;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
public class DeliveryInfoPersister extends ProcessorFunctionAdapter<List<DeliveryInfo>,List<DeliveryInfo>> {

    @Autowired
    private DeliveryInfoRepository deliveryInfoRepo;

	@Override
	protected List<DeliveryInfo> execute(List<DeliveryInfo> deliveryInfos) {
		List<DeliveryInfo> deliveryInfosInDB = new ArrayList<>();
		
		
		deliveryInfos.forEach(deliveryInfo -> {
			if (deliveryInfo.isNew()) {
				deliveryInfo.setId(UUID.randomUUID());
			}
			
			DeliveryInfo deliveryInfoInDB = deliveryInfoRepo.save(deliveryInfo);
			deliveryInfosInDB.add(deliveryInfoInDB);
		});

		
		return deliveryInfosInDB;
	}	

}
