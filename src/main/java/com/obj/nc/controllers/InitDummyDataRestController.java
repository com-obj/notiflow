package com.obj.nc.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromIntentGenerator;
import com.obj.nc.repositories.*;
import com.obj.nc.utils.JsonUtils;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.*;

@RestController
@RequestMapping("/init-dummy-data")
@RequiredArgsConstructor
public class InitDummyDataRestController {
    
    private final MessagesFromIntentGenerator messagesFromIntentGenerator;
    private final GenericEventRepository genericEventRepository;
    private final NotificationIntentRepository notificationIntentRepository;
    private final EndpointsRepository endpointsRepository;
    private final MessageRepository messageRepository;
    private final DeliveryInfoRepository deliveryInfoRepository;
    
    @GetMapping
    public void initDummyData() {
        GenericEvent event = persistEvent();
        List<RecievingEndpoint> receivingEndpoints = persistReceivingEndpoints();
        NotificationIntent notificationIntent = persistNotificationIntent(event, receivingEndpoints);
        
        List<Message<?>> messages = Lists
                .newArrayList(messageRepository.saveAll(createMessages(notificationIntent)))
                .stream()
                .map(MessagePersistantState::toMessage)
                .map(message -> (Message<?>) message)
                .collect(Collectors.toList());
        
        persistDeliveryInfos(messages);
        
    }
    
    private GenericEvent persistEvent() {
        return genericEventRepository.save(
                GenericEvent.builder()
                        .flowId("default-flow")
                        .payloadJson(
                                DummyEventPayload.builder()
                                        .stringField("simple string")
                                        .intField(15)
                                        .build()
                                        .toJsonNode()
                        )
                        .build()
        );
    }
    
    private List<RecievingEndpoint> persistReceivingEndpoints() {
        List<RecievingEndpoint> receivingEndpoints = Lists.newArrayList(
                EmailEndpoint.builder()
                        .email("john.doe@objectify.sk")
                        .build(),
                EmailEndpoint.builder()
                        .email("john.dudly@objectify.sk")
                        .build(),
                SmsEndpoint.builder()
                        .phone("0918111111")
                        .build()
        );
        return Lists.newArrayList(endpointsRepository.saveAll(receivingEndpoints));
    }
    
    private NotificationIntent persistNotificationIntent(GenericEvent event,
                                                         List<RecievingEndpoint> receivingEndpoints) {
        NotificationIntent intent = NotificationIntent.builder()
                .recievingEndpoints(receivingEndpoints)
                .build();
        intent.getHeader().addEventId(event.getId());
        return notificationIntentRepository.save(intent);
    }
    
    private List<MessagePersistantState> createMessages(NotificationIntent notificationIntent) {
        return messagesFromIntentGenerator
                .apply(notificationIntent)
                .stream()
                .map(Message::toPersistantState)
                .collect(Collectors.toList());
    }
    
    private void persistDeliveryInfos(List<Message<?>> messages) {
        for (int i = 0; i < messages.size() / 2; i++) {
            persistDeliveryInfosSuccessAndRead(messages.get(i));
        }
        
        for (int i = messages.size() / 2; i < messages.size(); i++) {
            persistDeliveryInfosFailed(messages.get(i));
        }
    }
    
    private void persistDeliveryInfosSuccessAndRead(Message<?> message) {
        persistMessageStatus(message, PROCESSING);
        persistMessageStatus(message, SENT);
        persistMessageStatus(message, READ);
    }
    
    private void persistDeliveryInfosFailed(Message<?> message) {
        persistMessageStatus(message, PROCESSING);
        persistMessageStatus(message, FAILED);
    }
    
    private void persistMessageStatus(Message<?> message, DELIVERY_STATUS status) {
        deliveryInfoRepository.save(
                DeliveryInfo.builder()
                        .endpointId(message.getRecievingEndpoints().get(0).getId())
                        .eventId(message.getEventIds().get(0))
                        .messageId(message.getMessageId())
                        .status(status)
                        .build()
        );
    }
    
    @Value
    @Builder
    private static class DummyEventPayload {
        String stringField;
        int intField;
        
        JsonNode toJsonNode() {
            return JsonUtils.readJsonNodeFromPojo(this);
        }
    }
    
}
