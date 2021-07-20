package com.obj.nc.controllers;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    
    @GetMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<EndpointDto> findAllEndpoints() {
        List<RecievingEndpoint> receivingEndpoints = endpointRepository.findAll();
        
        List<EndpointDto> endpointDtos = EndpointDto.from(receivingEndpoints);
        endpointDtos.forEach(
                endpointDto -> {
                    List<DeliveryInfo> endpointDeliveryInfos = deliveryInfoRepository.findByEndpointIdOrderByProcessedOn(endpointDto.getId());
                    List<MessagesPerStatus> messagesPerStatuses = MessagesPerStatus.from(endpointDeliveryInfos);
                    endpointDto.setMessagesPerStatus(messagesPerStatuses);
                });
        
        return endpointDtos;
    }
    
    @Data
    @Builder
    public static class EndpointDto {
        private UUID id;
        private String name;
        private String type;
        private List<MessagesPerStatus> messagesPerStatus;
    
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
    public static class MessagesPerStatus {
        private DELIVERY_STATUS status;
        private Integer count;
        
        public static List<MessagesPerStatus> from(List<DeliveryInfo> deliveryInfos) {
            Map<UUID, LinkedList<DeliveryInfo>> infosByMessage = groupInfosByMessage(deliveryInfos);
            
            Map<DELIVERY_STATUS, Integer> messagesPerStatus = countMessagesPerStatus(infosByMessage);
    
            return messagesPerStatus.entrySet().stream()
                    .map(
                            messagesPerStatusEntry -> MessagesPerStatus.builder()   
                            .status(messagesPerStatusEntry.getKey())
                            .count(messagesPerStatusEntry.getValue())
                            .build()
                    )
                    .collect(Collectors.toList());
        }
    
        private static Map<DELIVERY_STATUS, Integer> countMessagesPerStatus(Map<UUID, LinkedList<DeliveryInfo>> infosByMessage) {
            Map<DELIVERY_STATUS, Integer> messagesPerStatus = new HashMap<>();
            
            infosByMessage.values().forEach(
                    (infos) -> {
                        DeliveryInfo lastInfo = infos.getLast();
                        if (!messagesPerStatus.containsKey(lastInfo.getStatus())) {
                            messagesPerStatus.put(lastInfo.getStatus(), 0);
                        }
                        messagesPerStatus.put(lastInfo.getStatus(), messagesPerStatus.get(lastInfo.getStatus()) + 1);
                    }
            );
            
            return messagesPerStatus;
        }
    
        private static Map<UUID, LinkedList<DeliveryInfo>> groupInfosByMessage(List<DeliveryInfo> deliveryInfos) {
            return deliveryInfos.stream()
                    .collect(groupingBy(DeliveryInfo::getMessageId, LinkedHashMap::new, toCollection(LinkedList::new)));
        }
    }
}
