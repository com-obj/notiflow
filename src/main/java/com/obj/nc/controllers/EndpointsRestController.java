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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
    
    @GetMapping(value = "/all", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<EndpointDto> findAllEndpoints() {
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
    
    @GetMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<EndpointDto> findEndpointsWithMessageTypeInDateRange(
            @RequestParam(value = "startAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startAtParam, 
            @RequestParam(value = "endAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endAtParam,
            @RequestParam(value = "messageType", required = false) String messageTypeParam,
            @RequestParam(value = "deliveryStatus", required = false) DELIVERY_STATUS statusParam) {
        
        Instant startAt = startAtParam == null ? Instant.MIN : startAtParam;
        Instant endAt = endAtParam == null ? Instant.MAX : endAtParam;
        
        return findAllEndpoints().stream()
                .filter(endpoint -> endpointMatchesMessageType(endpoint, messageTypeParam))
                .filter(endpoint -> endpointHasMessageMatchingConditions(endpoint, startAt, endAt, statusParam))
                .collect(Collectors.toList());
    }
    
    private boolean endpointMatchesMessageType(EndpointDto endpoint, String messageType) {
        return messageType == null || messageType.equals(endpoint.getType());
    }
    
    private boolean endpointHasMessageMatchingConditions(EndpointDto endpoint, Instant startAt, Instant endAt, DELIVERY_STATUS status) {
        return endpoint.getInfosPerStatus().stream()
                .filter(infosPerStatus -> status == null || status.equals(infosPerStatus.getStatus()))
                .flatMap(infosPerStatus -> infosPerStatus.getDeliveryInfos().stream())
                .anyMatch(deliveryInfo -> startAt.isBefore(deliveryInfo.getProcessedOn()) && endAt.isAfter(deliveryInfo.getProcessedOn()));
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
                    .collect(Collectors.toList());
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
                    .collect(Collectors.toList());
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
                    .collect(groupingBy(DeliveryInfo::getMessageId, LinkedHashMap::new, toCollection(LinkedList::new)));
        }
    }
    
}
