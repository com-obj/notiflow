package com.obj.nc.functions.sink.deliveryInfoPersister;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.functions.processors.senders.dtos.DeliveryInfoSendResult;
import com.obj.nc.functions.sink.SinkConsumerAdapter;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
public class DeliveryInfoPersister extends SinkConsumerAdapter<DeliveryInfoSendResult> {

    @Autowired
    private DeliveryInfoRepository delInfoRepo;
    @Autowired
    private EndpointsRepository endpointsRepo;


	@Override
	protected void execute(DeliveryInfoSendResult deliveryInfo) {
		endpointsRepo.persistEnpointIfNotExists(deliveryInfo.getRecievingEndpoint());
		
		List<DeliveryInfo> infos = createFromSendResults(deliveryInfo);
		
		infos.forEach(info -> {
			info.setId(UUID.randomUUID());
			
			delInfoRepo.save(info);
		});
	}


	private List<DeliveryInfo> createFromSendResults(DeliveryInfoSendResult sendResult) {
		List<DeliveryInfo> resultInfos = new ArrayList<>();
		
		for (UUID eventId: sendResult.getEventIds()) {

			DeliveryInfo info = DeliveryInfo.builder()
				.processedOn(sendResult.getProcessedOn())
				.endpointId(sendResult.getRecievingEndpoint().getEndpointId())
				.id(UUID.randomUUID())
				.eventId(eventId)
				.status(sendResult.getStatus())
				.build();
			
			resultInfos.add(info);

		}
		return resultInfos;
	}
	

}
