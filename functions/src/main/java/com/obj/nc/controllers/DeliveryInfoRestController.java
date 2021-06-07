package com.obj.nc.controllers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.GenericEventRepository;

import lombok.Builder;
import lombok.Data;

@Validated
@RestController
@RequestMapping("/delivery-info")
public class DeliveryInfoRestController {
	
	
	@Autowired private DeliveryInfoRepository deliveryRepo;
	@Autowired private EndpointsRepository endpointRepo;
	@Autowired private GenericEventRepository eventRepo;
	
	@GetMapping(value = "/events/{eventId}", consumes="application/json", produces="application/json")
    public List<EndpointDeliveryInfoDto> findDeliveryInfosByEventId(
    		@PathVariable (value = "eventId", required = true) String eventId) {

		List<DeliveryInfo> deliveryInfos = deliveryRepo.findByEventIdOrderByProcessedOn(UUID.fromString(eventId));

		List<EndpointDeliveryInfoDto> infoDtos =  EndpointDeliveryInfoDto.createFrom(deliveryInfos);
		
		List<String> endpointIds = infoDtos.stream().map(i -> i.getEndpointId()).collect(Collectors.toList());
		List<RecievingEndpoint> endpoints = endpointRepo.findByIds(endpointIds.toArray(new String[0]));
		Map<String, EndpointDeliveryInfoDto> endpointsById = infoDtos.stream().collect(Collectors.toMap(EndpointDeliveryInfoDto::getEndpointId, info->info));
		endpoints.forEach(re-> endpointsById.get(re.getEndpointId()).setEndpoint(re));
		
		return infoDtos;
    }
	
	@GetMapping(value = "/events/ext/{extEventId}", consumes="application/json", produces="application/json")
    public List<EndpointDeliveryInfoDto> findDeliveryInfosByExtId(
    		@PathVariable (value = "extEventId", required = true) String extEventId) {

		GenericEvent event = eventRepo.findByExternalId(extEventId);
		if (event == null) {
			throw new IllegalArgumentException("Event with " +  extEventId +" external ID not found");
		}
		
		return findDeliveryInfosByEventId(event.getId().toString());
    }


	@Data
	@Builder
	public static class EndpointDeliveryInfoDto {
		
		@JsonIgnore
		@Transient
		String endpointId;
		
		RecievingEndpoint endpoint;
		
		DELIVERY_STATUS currentStatus;
		Instant statusReachedAt;
		
		public static List<EndpointDeliveryInfoDto> createFrom(List<DeliveryInfo> deliveryInfos) {
			List<EndpointDeliveryInfoDto> result = new ArrayList<>();
			
			Map<String, List<DeliveryInfo>> ep2Infos = deliveryInfos.stream().collect(
					Collectors.groupingBy(
							DeliveryInfo::getEndpointId,
							HashMap::new,
							Collectors.mapping(info->info, Collectors.toList()
					)
				)
			);
			
			for (String endpointId: ep2Infos.keySet()) {
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
