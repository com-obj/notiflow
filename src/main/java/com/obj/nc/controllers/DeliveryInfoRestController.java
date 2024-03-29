/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.config.PagingConfigProperties;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.dto.content.MessageContentDto;
import com.obj.nc.domain.dto.endpoint.ReceivingEndpointDto;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.domain.pagination.ResultPage;
import com.obj.nc.flows.deliveryInfo.DeliveryInfoFlow;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.MessageRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.obj.nc.utils.PagingUtils.createPageRequest;

@Slf4j
@Validated
@RestController
@RequestMapping("/delivery-info")
public class DeliveryInfoRestController {

	@Autowired private DeliveryInfoRepository deliveryRepo;
	@Autowired private EndpointsRepository endpointRepo;
	@Autowired private GenericEventRepository eventRepo;
    @Autowired private MessageRepository messageRepo;
	@Autowired private DeliveryInfoFlow deliveryInfoFlow;
	@Autowired private NcAppConfigProperties ncAppConfigProperties;
	@Autowired private PagingConfigProperties pagingConfigProperties;

	@GetMapping(value = "/events/{eventId}", produces="application/json")
	public ResultPage<EndpointDeliveryInfoDto> findDeliveryInfosByEventId(
			@PathVariable (value = "eventId", required = true) String eventId,
			@RequestParam(value = "endpointId", required = false) String endpointId,
			@RequestParam("page") int page,
			@RequestParam("size") int size
	) {
		Pageable pageable = createPageRequest(page, size, pagingConfigProperties);

		UUID endpointUUID = endpointId == null ? null : UUID.fromString(endpointId);

		long total = deliveryRepo.countByEventIdAndEndpointId(UUID.fromString(eventId), endpointUUID);
		List<DeliveryInfo> deliveries = deliveryRepo.findLatestByEventIdAndEndpointIdOrderByProcessedOn(
				UUID.fromString(eventId), endpointUUID, pageable.getPageSize(), pageable.getOffset());

		if (deliveries.isEmpty()) {
			log.warn("Failed to map event {} to delivery info. This indicate problem with data - probably no message exists for event.", eventId);
			return new ResultPage<>(Collections.emptyList());
		}

		UUID[] endpointIds = deliveries.stream().map(DeliveryInfo::getEndpointId).toArray(UUID[]::new);
		List<UUID> messageIds = deliveries.stream().map(DeliveryInfo::getMessageId).collect(Collectors.toList());

		List<ReceivingEndpoint> endpoints = endpointRepo.findByIds(endpointIds);
		List<MessagePersistentState> messages = messageRepo.findByIdIn(messageIds);

		List<EndpointDeliveryInfoDto> result = deliveries.stream()
				.map(delivery -> {
					UUID epId = delivery.getEndpointId();
					UUID msgId = delivery.getMessageId();

					Optional<ReceivingEndpoint> ep = endpoints.stream().filter(it -> it.getId().equals(epId)).findFirst();
					Optional<MessagePersistentState> msg = messages.stream().filter(it -> it.getId().equals(msgId)).findFirst();

					return EndpointDeliveryInfoDto.builder()
							.endpoint(ep.map(ReceivingEndpoint::toDto).orElse(null))
							.message(msg.map(MessagePersistentState::getBody).map(MessageContent::toDto).orElse(null))
							.currentStatus(delivery.getStatus())
							.statusReachedAt(delivery.getProcessedOn())
							.additionalInformation(delivery.getAdditionalInformation())
							.build();
				})
				.collect(Collectors.toList());

		return new ResultPage<>(result, pageable, total);
	}

	@GetMapping(value = "/events/ext/{extEventId}", produces="application/json")
	public ResultPage<EndpointDeliveryInfoDto> findDeliveryInfosByExtId(
			@PathVariable (value = "extEventId", required = true) String extEventId,
			@RequestParam(value = "endpointId", required = false) String endpointId,
			@RequestParam("page") int page,
			@RequestParam("size") int size
	) {

		GenericEvent event = eventRepo.findByExternalId(extEventId);
		if (event == null) {
			throw new IllegalArgumentException("Event with " +  extEventId +" external ID not found");
		}

		return findDeliveryInfosByEventId(event.getId().toString(), endpointId, page, size);
	}

	@GetMapping(value = "/messages/{messageId}/mark-as-read")
	public ResponseEntity<Void> trackMessageRead(@PathVariable(value = "messageId", required = true) String messageId) {
		ResponseEntity<Void> trackingPixelImageRedirectionResponse = ResponseEntity
				.status(HttpStatus.FOUND)
				.location(
						UriComponentsBuilder
								.fromPath(ncAppConfigProperties.getContextPath())
								.path("/resources/images/px.png")
								.build()
								.toUri())
				.build();

		if (deliveryRepo.countByMessageIdAndStatus(UUID.fromString(messageId), DELIVERY_STATUS.READ) > 0) {
			return trackingPixelImageRedirectionResponse;
		}

        MessagePersistentState msgInDb = messageRepo.findById(UUID.fromString(messageId)).get();
        Message<?> msg = msgInDb.toMessage();
        deliveryInfoFlow.createAndPersistReadDeliveryInfo(msg);
        
		return trackingPixelImageRedirectionResponse;
	}

	@GetMapping(value = "/messages/{messageId}", produces="application/json")
	public List<EndpointDeliveryInfoDto> findDeliveryInfosByMessageId(
			@PathVariable (value = "messageId", required = true) String messageId) {

		List<DeliveryInfo> deliveryInfos = deliveryRepo.findByMessageIdOrderByProcessedOn(UUID.fromString(messageId));

		List<EndpointDeliveryInfoDto> infoDtos =  EndpointDeliveryInfoDto.createFrom(deliveryInfos);

		List<UUID> endpointIds = infoDtos
            .stream()
            .map(i -> i.getEndpointId())
            .collect(Collectors.toList());
		List<ReceivingEndpoint> endpoints = endpointRepo.findByIds(endpointIds.toArray(new UUID[0]));

		Map<UUID, EndpointDeliveryInfoDto> endpointsById = infoDtos
            .stream()
            .collect(Collectors.toMap(EndpointDeliveryInfoDto::getEndpointId, info->info));

		endpoints.forEach(re-> endpointsById.get(re.getId()).setEndpoint(re.toDto()));

		return infoDtos;
	}

	@Data
	@Builder
	public static class EndpointDeliveryInfoDto {

		@JsonIgnore
		@Transient
		UUID endpointId;

		MessageContentDto message;
		ReceivingEndpointDto endpoint;
		DELIVERY_STATUS currentStatus;
		String additionalInformation;
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
					.additionalInformation(lastInfo.getAdditionalInformation())
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
