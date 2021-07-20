package com.obj.nc.controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping("/endpoints")
@RequiredArgsConstructor
public class EndpointsRestController {
    
    private final EndpointsRepository endpointRepository;
    private final DeliveryInfoRepository deliveryInfoRepository;
    
    @GetMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<EndpointDto> findEndpointsWithMessageTypeInDateRange(
            @RequestParam(value = "startAt", required = false) Instant startAt, 
            @RequestParam(value = "endAt", required = false) Instant endAt,
            @RequestParam(value = "messageType", required = false) String messageType,
            @RequestParam(value = "deliveryStatus", required = false) DELIVERY_STATUS deliveryStatus) {
        
        return findAllEndpoints().stream()
                .filter(endpoint -> endpoint.hasMessageInDateRange(startAt, endAt))
                .filter(endpoint -> endpoint.matchesMessageType(messageType))
                .filter(endpoint -> endpoint.hasMessageWithDeliveryStatus(deliveryStatus))
                .collect(toList());
    }
    
    private List<EndpointDto> findAllEndpoints() {
        List<RecievingEndpoint> receivingEndpoints = endpointRepository.findAll();
        
        List<EndpointDto> endpointDtos = EndpointDto.from(receivingEndpoints);
        endpointDtos.forEach(
                endpointDto -> {
                    List<DeliveryInfo> endpointDeliveryInfos = deliveryInfoRepository.findByEndpointIdOrderByProcessedOn(endpointDto.getId());
                    List<InfosPerStatus> infosPerStatuses = InfosPerStatus.from(endpointDeliveryInfos);
                    endpointDto.setInfosPerStatus(infosPerStatuses);
                });
        
        return endpointDtos;
    }
    
    @Data
    @Builder
    public static class EndpointDto {
        private UUID id;
        private String name;
        private String type;
        private List<InfosPerStatus> infosPerStatus;
    
        public static List<EndpointDto> from(List<RecievingEndpoint> receivingEndpoints) {
            return receivingEndpoints.stream()
                    .map(
                            receivingEndpoint -> EndpointDto.builder()
                                .id(receivingEndpoint.getId())
                                .name(receivingEndpoint.getEndpointId())
                                .type(receivingEndpoint.getEndpointType())
                                .build()
                    )
                    .collect(toList());
        }
    
        public boolean hasMessageInDateRange(Instant startAt, Instant endAt) {
            List<DeliveryInfo> deliveryInfos = getInfosPerStatus().stream()
                    .flatMap(infosPerStatus -> infosPerStatus.getDeliveryInfos().stream())
                    .collect(toList());
            
            boolean afterStart = startAt == null;
            afterStart |= startAt != null && deliveryInfos.stream()
                    .anyMatch(deliveryInfo -> startAt.isBefore(deliveryInfo.getProcessedOn()));
    
            boolean beforeEnd = endAt == null;
            beforeEnd |= endAt != null && deliveryInfos.stream()
                    .anyMatch(deliveryInfo -> endAt.isAfter(deliveryInfo.getProcessedOn()));
        
            return afterStart && beforeEnd;
        }
    
        public boolean matchesMessageType(String messageType) {
            return messageType == null || messageType.equals(getType());
        }
    
        public boolean hasMessageWithDeliveryStatus(DELIVERY_STATUS status) {
            boolean matchesStatus = status == null;
            matchesStatus |= status != null && getInfosPerStatus().stream()
                    .anyMatch(infosPerStatus -> status.equals(infosPerStatus.getStatus()));
            return matchesStatus;
        }
    }
    
    @Data
    @Builder
    public static class InfosPerStatus {
        private DELIVERY_STATUS status;
        
        @JsonIgnore
        private List<DeliveryInfo> deliveryInfos;
        
        @JsonProperty("count")
        public Integer getInfosCount() {
            return deliveryInfos.size();
        }
        
        public static List<InfosPerStatus> from(List<DeliveryInfo> deliveryInfos) {
            Map<UUID, LinkedList<DeliveryInfo>> infosByMessage = groupInfosByMessage(deliveryInfos);
            
            Map<DELIVERY_STATUS, List<DeliveryInfo>> infosByStatus = groupLastInfosByStatus(infosByMessage);
    
            return infosByStatus.entrySet().stream()
                    .map(
                            infosByStatusEntry -> InfosPerStatus.builder()   
                                .status(infosByStatusEntry.getKey())
                                .deliveryInfos(infosByStatusEntry.getValue())
                                .build()
                    )
                    .collect(toList());
        }
    
        private static Map<DELIVERY_STATUS, List<DeliveryInfo>> groupLastInfosByStatus(Map<UUID, LinkedList<DeliveryInfo>> infosByMessage) {
            Map<DELIVERY_STATUS, List<DeliveryInfo>> messagesByStatus = new HashMap<>();
            
            infosByMessage.values().forEach(
                    infos -> {
                        DeliveryInfo lastInfo = infos.getLast();
                        if (!messagesByStatus.containsKey(lastInfo.getStatus())) {
                            messagesByStatus.put(lastInfo.getStatus(), new ArrayList<>());
                        }
                        messagesByStatus.get(lastInfo.getStatus()).add(lastInfo);
                    }
            );
            
            return messagesByStatus;
        }
    
        private static Map<UUID, LinkedList<DeliveryInfo>> groupInfosByMessage(List<DeliveryInfo> deliveryInfos) {
            return deliveryInfos.stream()
                    .filter(deliveryInfo -> deliveryInfo.getMessageId() != null)
                    .collect(groupingBy(DeliveryInfo::getMessageId, LinkedHashMap::new, toCollection(LinkedList::new)));
        }
    }
    
}
