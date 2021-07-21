package com.obj.nc.controllers;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.flows.deliveryInfo.DeliveryInfoFlow;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.GenericEventRepository;

import lombok.Builder;
import lombok.Data;

@Validated
@RestController
@RequestMapping("/delivery-info")
public class DeliveryInfoRestController {
	
	@Autowired private DeliveryInfoRepository deliveryRepo;
	@Autowired private MessageRepository messageRepo;
	@Autowired private EndpointsRepository endpointRepo;
	@Autowired private GenericEventRepository eventRepo;
	@Autowired private DeliveryInfoFlow deliveryInfoFlow;
	
	@GetMapping(value = "/events/{eventId}", consumes="application/json", produces="application/json")
    public List<EndpointDeliveryInfoDto> findDeliveryInfosByEventId(
    		@PathVariable (value = "eventId", required = true) String eventId,
			Pageable pageable) {

		List<DeliveryInfo> deliveryInfos = deliveryRepo.findByEventIdOrderByProcessedOn(UUID.fromString(eventId));

		List<EndpointDeliveryInfoDto> infoDtos =  EndpointDeliveryInfoDto.createFrom(deliveryInfos);
		
		List<UUID> endpointIds = infoDtos.stream().map(i -> i.getEndpointId()).collect(Collectors.toList());
		List<RecievingEndpoint> endpoints = endpointRepo.findEndpointsByIds(pageable, endpointIds);
		Map<UUID, EndpointDeliveryInfoDto> endpointsById = infoDtos.stream().collect(Collectors.toMap(EndpointDeliveryInfoDto::getEndpointId, info->info));
		endpoints.forEach(re-> endpointsById.get(re.getId()).setEndpoint(re));
		
		return infoDtos;
    }
	
	@GetMapping(value = "/events/ext/{extEventId}", consumes="application/json", produces="application/json")
    public List<EndpointDeliveryInfoDto> findDeliveryInfosByExtId(
    		@PathVariable (value = "extEventId", required = true) String extEventId,
			Pageable pageable) {

		GenericEvent event = eventRepo.findByExternalId(extEventId);
		if (event == null) {
			throw new IllegalArgumentException("Event with " +  extEventId +" external ID not found");
		}
		
		return findDeliveryInfosByEventId(event.getId().toString(), pageable);
    }
	
	@PutMapping(value = "/messages/read/{messageId}")
	public ResponseEntity<Void> trackMessageRead(@PathVariable(value = "messageId", required = true) String messageId) {
		Optional<MessagePersistantState> message = messageRepo.findById(UUID.fromString(messageId));
		message.ifPresent(messagePersistantState -> deliveryInfoFlow.createAndPersistReadDeliveryInfo(messagePersistantState.toMessage()));
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/resources/images/px.png")).build();
	}

	@Data
	@Builder
	public static class EndpointDeliveryInfoDto {
		
		@JsonIgnore
		@Transient
		UUID endpointId;
		
		RecievingEndpoint endpoint;
		
		DELIVERY_STATUS currentStatus;
		Instant statusReachedAt;
		
		public static List<EndpointDeliveryInfoDto> createFrom(List<DeliveryInfo> deliveryInfos) {
			List<EndpointDeliveryInfoDto> result = new ArrayList<>();
			
			Map<UUID, List<DeliveryInfo>> ep2Infos = deliveryInfos.stream().collect(
					Collectors.groupingBy(
							DeliveryInfo::getEndpointId,
							HashMap::new,
							Collectors.mapping(info->info, Collectors.toList()
					)
				)
			);
			
			for (UUID endpointId: ep2Infos.keySet()) {
				List<DeliveryInfo> infos = ep2Infos.get(endpointId);
				orderByProcessedTimeDescStatus(infos);
				DeliveryInfo lastInfo = infos.get(infos.size()-1);
				
				EndpointDeliveryInfoDto dto = EndpointDeliveryInfoDto.builder()
					.currentStatus(lastInfo.getStatus())
					.endpointId(endpointId)
					.statusReachedAt(lastInfo.getProcessedOn())
					.build();
				
				result.add(dto);
			}
			
			return result;
		}

		private static void orderByProcessedTimeDescStatus(List<DeliveryInfo> infos) {
			Collections.sort(infos, (i1,i2) -> {
				int timeBased = i1.getProcessedOn().compareTo(i2.getProcessedOn());
				int statusBased = i1.getStatus().compareTo(i2.getStatus()) *1;
				return timeBased == 0 ?  statusBased: timeBased;
			});
		}
	}


}
