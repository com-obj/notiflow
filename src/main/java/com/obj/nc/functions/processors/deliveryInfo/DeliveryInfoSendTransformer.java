package com.obj.nc.functions.processors.deliveryInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.senders.dtos.DeliveryInfoSendResult;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
public class DeliveryInfoSendTransformer extends ProcessorFunctionAdapter<DeliveryInfoSendResult, List<DeliveryInfo>> {

	@Override
	protected List<DeliveryInfo> execute(DeliveryInfoSendResult deliveryInfo) {
		List<DeliveryInfo> infos = createFromSendResults(deliveryInfo);
				
		infos.forEach(info -> {
			info.setId(UUID.randomUUID());
		});
		
		return infos;
	}

	private List<DeliveryInfo> createFromSendResults(DeliveryInfoSendResult sendResult) {
		List<DeliveryInfo> resultInfos = new ArrayList<>();
		
		for (UUID eventId: sendResult.getEventIds()) {
			DeliveryInfo info = DeliveryInfo.builder()
					.endpointId(sendResult.getRecievingEndpoint().getId())
					.eventId(eventId)
					.status(sendResult.getStatus())
					.build();
			resultInfos.add(info);
		}
		
		for (UUID intentId: sendResult.getIntentIds()) {
			DeliveryInfo info = DeliveryInfo.builder()
					.endpointId(sendResult.getRecievingEndpoint().getId())
					.intentId(intentId)
					.status(sendResult.getStatus())
					.build();
			resultInfos.add(info);
		}
		
		for (UUID messageId: sendResult.getMessageIds()) {
			DeliveryInfo info = DeliveryInfo.builder()
					.endpointId(sendResult.getRecievingEndpoint().getId())
					.messageId(messageId)
					.status(sendResult.getStatus())
					.build();
			resultInfos.add(info);
		}
		
		return resultInfos;
	}
	
}
