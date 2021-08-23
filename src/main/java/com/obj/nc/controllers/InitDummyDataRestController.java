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
import com.obj.nc.flows.errorHandling.domain.FailedPaylod;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromIntentGenerator;
import com.obj.nc.repositories.*;
import com.obj.nc.utils.JsonUtils;
import lombok.*;
import org.springframework.integration.history.MessageHistory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
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
    private final FailedPayloadRepository failedPayloadRepository;
    
    @GetMapping
    public void initDummyData() {
        GenericEvent event = persistEvent();
        List<RecievingEndpoint> receivingEndpoints = persistReceivingEndpoints();
        NotificationIntent notificationIntent = persistNotificationIntent(event, receivingEndpoints);
        List<Message<?>> messages = persistMessages(notificationIntent);
        persistDeliveryInfos(messages);
    }
    
    private GenericEvent persistEvent() {
        return genericEventRepository.save(
                GenericEvent.builder()
                        .id(UUID.randomUUID())
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
        return Lists.newArrayList(endpointsRepository.persistEnpointIfNotExists(receivingEndpoints));
    }
    
    private NotificationIntent persistNotificationIntent(GenericEvent event,
                                                         List<RecievingEndpoint> receivingEndpoints) {
        NotificationIntent intent = NotificationIntent
                .createWithStaticContent(
                        "subject", 
                        "body", 
                        receivingEndpoints.toArray(new RecievingEndpoint[0]));
        intent.getHeader().addEventId(event.getId());
        return notificationIntentRepository.save(intent);
    }
    
    private List<Message<?>> persistMessages(NotificationIntent notificationIntent) {
        List<MessagePersistantState> messages = messagesFromIntentGenerator
                .apply(notificationIntent)
                .stream()
                .map(Message::toPersistantState)
                .collect(Collectors.toList());
        
        List<MessagePersistantState> savedMessages = Lists.newArrayList(messageRepository.saveAll(messages));
        
        return savedMessages
                .stream()
                .map(MessagePersistantState::toMessage)
                .map(message -> (Message<?>) message)
                .collect(Collectors.toList());
    }
    
    private void persistDeliveryInfos(List<Message<?>> messages) {
        for (int i = 0; i <= messages.size() / 2; i++) {
            persistDeliveryInfosSuccessAndRead(messages.get(i));
        }
        
        for (int i = messages.size() / 2 + 1; i < messages.size(); i++) {
            persistDeliveryInfosFailed(messages.get(i));
        }
    }
    
    private void persistDeliveryInfosSuccessAndRead(Message<?> message) {
        persistMessageStatus(message, PROCESSING);
        persistMessageStatus(message, SENT);
        persistMessageStatus(message, READ);
    }
    
    private void persistDeliveryInfosFailed(Message<?> message) {
        persistMessageStatus(message, FAILED);
        
        org.springframework.messaging.Message<?> failedSpringMessage = MessageBuilder
                .withPayload(message)
                .build();
        JsonNode failedMessageJson = JsonUtils.readJsonNodeFromPojo(failedSpringMessage);
        
        FailedPaylod failedPayload = FailedPaylod.builder()
                .id(UUID.randomUUID())
                .flowId("default-flow")
                .channelNameForRetry("channel")
                .messageJson(failedMessageJson)
                .build();
        failedPayload.setAttributesFromException(new IllegalArgumentException("illegal argument"));
        
        failedPayloadRepository.save(failedPayload);
    }
    
    private void persistMessageStatus(Message<?> message, DELIVERY_STATUS status) {
        deliveryInfoRepository.save(
                DeliveryInfo.builder()
                        .id(UUID.randomUUID())
                        .endpointId(message.getRecievingEndpoints().get(0).getId())
                        .eventId(message.getEventIds().get(0))
                        .messageId(message.getMessageId())
                        .status(status)
                        .build()
        );
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class DummyEventPayload {
        String stringField;
        int intField;
        
        JsonNode toJsonNode() {
            return JsonUtils.readJsonNodeFromPojo(this);
        }
    }
    
}
