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

import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.FAILED;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.PROCESSING;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.SENT;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.flows.errorHandling.domain.FailedPayload;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.FailedPayloadRepository;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.repositories.NotificationIntentRepository;
import com.obj.nc.repositories.ProcessingInfoRepository;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/test-data")
@RequiredArgsConstructor
public class TestDataRestController {
    
    private final GenericEventRepository genericEventRepository;
    private final NotificationIntentRepository notificationIntentRepository;
    private final EndpointsRepository endpointsRepository;
    private final MessageRepository messageRepository;
    private final DeliveryInfoRepository deliveryInfoRepository;
    private final FailedPayloadRepository failedPayloadRepository;
    private final ProcessingInfoRepository processingInfoRepository;
    
    @GetMapping("/full-event-processing")
    public void persistFullEventProcessingData() {
        persistEvent(
                UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d"), 
                47L,
                Timestamp.valueOf("2021-08-24 09:37:39.366000").toInstant(),
                Timestamp.valueOf("2021-08-24 09:37:38.413000").toInstant()
        );
        persistReceivingEndpoints();
        persistNotificationIntent();
        persistMessages();
        persistFailedPayload();
        persistDeliveryInfos();
        persistProcessingInfos();
    }
    
    @GetMapping("/random-events")
    public void persistRandomEventsData(@RequestParam(value = "count", defaultValue = "47") Long count) {
        Random random = new Random();
        random.longs().limit(count).forEach(randomLong -> {
            Instant randomInstant = randomInstantBetween(Instant.now().minus(365, ChronoUnit.DAYS), Instant.now());
            persistEvent(
                    UUID.randomUUID(),
                    randomLong,
                    randomInstant,
                    randomInstant.minus(5, ChronoUnit.SECONDS));
        });
    }
    
    private void persistEvent(UUID uuid, Long num, Instant timeConsumed, Instant timeCreated) {
        DummyEventPayload payload = DummyEventPayload.builder()
                .stringField("simple string")
                .longField(num)
                .build();
    
        GenericEvent event = GenericEvent.builder()
                .id(uuid)
                .flowId("default-flow")
                .payloadJson(payload.toJsonNode())
                .timeConsumed(timeConsumed)
                .build();
    
        event = genericEventRepository.save(event);
        event.setTimeCreated(timeCreated);
        genericEventRepository.save(event);
    }
    
    private void persistReceivingEndpoints() {
        EmailEndpoint johnDoeEmail = EmailEndpoint.builder().email("john.doe@objectify.sk").build();
        johnDoeEmail.setId(UUID.fromString("3d02890b-1b53-465c-b8c3-b0a722483a45"));
        saveEndpoint(johnDoeEmail, "2021-08-24 09:37:39.474000");
    
        EmailEndpoint johnDudlyEmail = EmailEndpoint.builder().email("john.dudly@objectify.sk").build();
        johnDudlyEmail.setId(UUID.fromString("d2ca2a68-1dc4-4052-a93a-67319449ef5b"));
        saveEndpoint(johnDudlyEmail, "2021-08-24 09:37:39.474000");
    
        SmsEndpoint phone = SmsEndpoint.builder().phone("0918111111").build();
        phone.setId(UUID.fromString("820023cf-7dd0-407f-abfa-0d6453bde6d3"));
        saveEndpoint(phone, "2021-08-24 09:37:39.474000");
    }
    
    private void saveEndpoint(ReceivingEndpoint endpoint, String timeString) {
        endpoint = endpointsRepository.persistEnpointIfNotExists(endpoint);
        endpoint.setTimeCreated(Timestamp.valueOf(timeString).toInstant());
        endpointsRepository.persistEnpointIfNotExists(endpoint);
    }
    
    private void persistNotificationIntent() {
        NotificationIntent intent = NotificationIntent.createWithStaticContent("Subject", "Text");
        intent.setId(UUID.fromString("ee0ee80a-bde8-4bc1-91a2-0abc1cfa39a9"));
        intent.getHeader().setFlowId("default-flow");
        intent.addPreviousEventId(UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d"));
        
        intent = notificationIntentRepository.save(intent);
        intent.setTimeCreated(Timestamp.valueOf("2021-08-24 09:37:39.490000").toInstant());
        notificationIntentRepository.save(intent);
    }
    
    private void persistMessages() {
        // Email 1
        MessagePersistentState message = new MessagePersistentState();
        message.setHeader(new Header());
        message.setId(UUID.fromString("5f74929b-2635-4515-9f30-ba79e200e92b"));
        message.getHeader().setFlowId("default-flow");
        message.setPreviousEventIds(new UUID[]{ UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d") });
        message.setBody(EmailContent.builder().subject("Subject").text("Text").build());
        message.setMessageClass(EmailMessage.class.getName());
        message.setEndpointIds(new UUID[]{ UUID.fromString("3d02890b-1b53-465c-b8c3-b0a722483a45") });
        
        message = messageRepository.save(message);
        message.setTimeCreated(Timestamp.valueOf("2021-08-24 09:37:39.540000").toInstant());
        messageRepository.save(message);
        // Email 2
        message = new MessagePersistentState();
        message.setHeader(new Header());
        message.setId(UUID.fromString("4e91137e-93aa-4633-b137-3050442d26a5"));
        message.getHeader().setFlowId("default-flow");
        message.setPreviousEventIds(new UUID[]{ UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d") });
        message.setBody(EmailContent.builder().subject("Subject").text("Text").build());
        message.setMessageClass(EmailMessage.class.getName());
        message.setEndpointIds(new UUID[]{ UUID.fromString("d2ca2a68-1dc4-4052-a93a-67319449ef5b") });
        
        message = messageRepository.save(message);
        message.setTimeCreated(Timestamp.valueOf("2021-08-24 09:37:39.575000").toInstant());
        messageRepository.save(message);
        // SMS 1
        message = new MessagePersistentState();
        message.setHeader(new Header());
        message.setId(UUID.fromString("c3ce484f-f03c-47b1-ae6a-2337ca4f6444"));
        message.getHeader().setFlowId("default-flow");
        message.setPreviousEventIds(new UUID[]{ UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d") });
        message.setBody(SimpleTextContent.builder().text("Text").build());
        message.setMessageClass(SmsMessage.class.getName());
        message.setEndpointIds(new UUID[]{ UUID.fromString("820023cf-7dd0-407f-abfa-0d6453bde6d3") });
        
        message = messageRepository.save(message);
        message.setTimeCreated(Timestamp.valueOf("2021-08-24 09:37:39.603000").toInstant());
        messageRepository.save(message);
    }
    
    private void persistFailedPayload() {
        FailedPayload failedPayload = FailedPayload.builder()
                .id(UUID.fromString("5469855e-450d-4ab5-a64e-4f6ad8f547a3"))
                .flowId("TODO")
                .exceptionName("org.springframework.messaging.core.DestinationResolutionException")
                .errorMessage("no output-channel or replyChannel header available")
                .rootCauseExceptionName("org.springframework.messaging.core.DestinationResolutionException")
                .stackTrace("org.springframework.messaging.core.DestinationResolutionException: no output-channel or replyChannel header available\n" +
                        "\tat org.springframework.integration.handler.AbstractMessageProducingHandler.sendOutput(AbstractMessageProducingHandler.java:445)\n" +
                        "\tat org.springframework.integration.handler.AbstractMessageProducingHandler.doProduceOutput(AbstractMessageProducingHandler.java:324)\n" +
                        "\tat org.springframework.integration.handler.AbstractMessageProducingHandler.produceOutput(AbstractMessageProducingHandler.java:267)\n" +
                        "\tat org.springframework.integration.handler.AbstractMessageProducingHandler.sendOutputs(AbstractMessageProducingHandler.java:231)\n" +
                        "\tat org.springframework.integration.handler.AbstractReplyProducingMessageHandler.handleMessageInternal(AbstractReplyProducingMessageHandler.java:140)\n" +
                        "\t...\n")
                .channelNameForRetry("MESSAGE_PROCESSING_FLOW_ID.channel#3")
                .messageJson(JsonUtils.readJsonNodeFromJSONString("{\"@class\": \"org.springframework.messaging.support.GenericMessage\", \"headers\": {\"id\": [\"java.util.UUID\", \"f797484f-58c1-8205-9f59-fe1ebffd431f\"], \"@class\": \"java.util.HashMap\", \"timestamp\": [\"java.lang.Long\", 1629797859649], \"sequenceSize\": 1, \"correlationId\": [\"java.util.UUID\", \"5a95a3ba-4642-837f-857b-3e9990b812bb\"], \"sequenceNumber\": 1, \"sequenceDetails\": [\"java.util.Collections$UnmodifiableRandomAccessList\", [[\"java.util.Arrays$ArrayList\", [[\"java.util.UUID\", \"9926bfa3-ee90-90ed-eb80-d288556a3f4d\"], 1, 1]], [\"java.util.Arrays$ArrayList\", [[\"java.util.UUID\", \"0a1eae1c-e6d1-951d-c407-8fd678086b84\"], 3, 3]]]]}, \"payload\": {\"id\": \"c3ce484f-f03c-47b1-ae6a-2337ca4f6444\", \"body\": {\"text\": \"Text\", \"@class\": \"com.obj.nc.domain.content.sms.SimpleTextContent\", \"attributes\": {\"@class\": \"java.util.HashMap\"}}, \"@class\": \"com.obj.nc.domain.message.SmsMessage\", \"header\": {\"@class\": \"com.obj.nc.domain.headers.Header\", \"flow-id\": \"default-flow\", \"eventIds\": [\"java.util.Arrays$ArrayList\", [\"e2c59478-6032-4bde-a8c1-0ce42248484d\"]], \"attributes\": {\"@class\": \"java.util.HashMap\"}, \"processingInfo\": {\"id\": \"8d70421d-e984-4a75-833c-5a06050af16f\", \"new\": true, \"@class\": \"com.obj.nc.domain.headers.ProcessingInfo\", \"version\": 0, \"eventIds\": [\"e2c59478-6032-4bde-a8c1-0ce42248484d\"], \"stepName\": \"MessageByRecipientTokenizer\", \"stepIndex\": 3, \"processingId\": \"8d70421d-e984-4a75-833c-5a06050af16f\", \"stepDurationMs\": 0, \"prevProcessingId\": \"8357b163-f148-4b05-aa23-8a78dd12116f\", \"timeProcessingEnd\": 1629797859.583, \"timeProcessingStart\": 1629797859.583}}, \"attributes\": {\"@class\": \"java.util.HashMap\"}, \"timeCreated\": 1629797859.603, \"receivingEndpoints\": [\"java.util.ArrayList\", [{\"id\": \"820023cf-7dd0-407f-abfa-0d6453bde6d3\", \"@type\": \"SMS\", \"phone\": \"0918111111\", \"recipient\": null, \"endpointId\": \"0918111111\", \"timeCreated\": null, \"deliveryOptions\": null}]]}}"))
                // set to forbid resurrection
                .timeResurected(Timestamp.valueOf("2021-08-24 09:38:39.682000").toInstant())
                .build();
        
        failedPayload = failedPayloadRepository.save(failedPayload);
        failedPayload.setTimeCreated(Timestamp.valueOf("2021-08-24 09:37:39.682000").toInstant());
        failedPayloadRepository.save(failedPayload);
    }
    
    private void persistDeliveryInfos() {
        DeliveryInfo deliveryInfo = DeliveryInfo.builder()
                .status(PROCESSING)
                .id(UUID.fromString("2506d56e-4cdd-4bca-bfb7-c7adb95255ce"))
                .eventId(UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d"))
                .version(0)
                .failedPayloadId(null)
                .endpointId(UUID.fromString("3d02890b-1b53-465c-b8c3-b0a722483a45"))
                .build();
    
        deliveryInfo = deliveryInfoRepository.save(deliveryInfo);
        deliveryInfo.setProcessedOn(Timestamp.valueOf("2021-08-24 09:37:39.645000").toInstant());
        deliveryInfoRepository.save(deliveryInfo);
    
        deliveryInfo = DeliveryInfo.builder()
                .status(PROCESSING)
                .id(UUID.fromString("1e6be6ad-81af-453f-a595-9265f3c0a8c5"))
                .eventId(UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d"))
                .version(0)
                .failedPayloadId(null)
                .endpointId(UUID.fromString("d2ca2a68-1dc4-4052-a93a-67319449ef5b"))
                .build();
    
        deliveryInfo = deliveryInfoRepository.save(deliveryInfo);
        deliveryInfo.setProcessedOn(Timestamp.valueOf("2021-08-24 09:37:39.637000").toInstant());
        deliveryInfoRepository.save(deliveryInfo);
    
        deliveryInfo = DeliveryInfo.builder()
                .status(PROCESSING)
                .id(UUID.fromString("6d22d1a7-fe9b-4841-983d-a7e9bbc809a0"))
                .eventId(UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d"))
                .version(0)
                .failedPayloadId(null)
                .endpointId(UUID.fromString("820023cf-7dd0-407f-abfa-0d6453bde6d3"))
                .build();
    
        deliveryInfo = deliveryInfoRepository.save(deliveryInfo);
        deliveryInfo.setProcessedOn(Timestamp.valueOf("2021-08-24 09:37:39.667000").toInstant());
        deliveryInfoRepository.save(deliveryInfo);
    
        deliveryInfo = DeliveryInfo.builder()
                .status(SENT)
                .id(UUID.fromString("6ab79ba7-ca42-4851-bab7-56c775e5b424"))
                .eventId(UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d"))
                .version(0)
                .failedPayloadId(null)
                .endpointId(UUID.fromString("3d02890b-1b53-465c-b8c3-b0a722483a45"))
                .build();
    
        deliveryInfo = deliveryInfoRepository.save(deliveryInfo);
        deliveryInfo.setProcessedOn(Timestamp.valueOf("2021-08-24 09:37:39.737000").toInstant());
        deliveryInfoRepository.save(deliveryInfo);
    
        deliveryInfo = DeliveryInfo.builder()
                .status(SENT)
                .id(UUID.fromString("07960564-d274-408c-89c9-fd16967b97f7"))
                .eventId(UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d"))
                .version(0)
                .failedPayloadId(null)
                .endpointId(UUID.fromString("d2ca2a68-1dc4-4052-a93a-67319449ef5b"))
                .build();
    
        deliveryInfo = deliveryInfoRepository.save(deliveryInfo);
        deliveryInfo.setProcessedOn(Timestamp.valueOf("2021-08-24 09:37:39.746000").toInstant());
        deliveryInfoRepository.save(deliveryInfo);
    
        deliveryInfo = DeliveryInfo.builder()
                .status(FAILED)
                .id(UUID.fromString("7d80114f-cf90-4c3f-8fce-850556604e1d"))
                .eventId(UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d"))
                .version(0)
                .failedPayloadId(UUID.fromString("5469855e-450d-4ab5-a64e-4f6ad8f547a3"))
                .endpointId(UUID.fromString("820023cf-7dd0-407f-abfa-0d6453bde6d3"))
                .build();
    
        deliveryInfo = deliveryInfoRepository.save(deliveryInfo);
        deliveryInfo.setProcessedOn(Timestamp.valueOf("2021-08-24 09:37:39.744000").toInstant());
        deliveryInfoRepository.save(deliveryInfo);
    }
    
    private void persistProcessingInfos() {
        ProcessingInfo processingInfo = ProcessingInfo.builder()
                .processingId(UUID.fromString("5ebf5469-6459-471d-93f7-e6a09702fabf"))
                .eventIds(new UUID[]{ UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d") })
                .prevProcessingId(null)
                .stepName("InputEventSupplier")
                .stepIndex(0)
                .timeProcessingStart(Timestamp.valueOf("2021-08-24 09:37:39.348000").toInstant())
                .timeProcessingEnd(Timestamp.valueOf("2021-08-24 09:37:39.387000").toInstant())
                .stepDurationMs(39)
                .payloadJsonStart(null)
                .payloadJsonEnd("{\"id\":\"e2c59478-6032-4bde-a8c1-0ce42248484d\",\"flowId\":\"default-flow\",\"payloadType\":null,\"payloadJson\":{\"@class\":\"com.obj.nc.flows.inpuEventRouting.DummyExtensionBasedEventConverterTests$DummyEventPayload\",\"intField\":15,\"stringField\":\"simple string\"},\"externalId\":null,\"timeCreated\":{\"epochSecond\":1629797858,\"nano\":413000000},\"timeConsumed\":{\"epochSecond\":1629797859,\"nano\":366000000}}")
                .version(0)
                .build();
        processingInfoRepository.save(processingInfo);
    
        processingInfo = ProcessingInfo.builder()
                .processingId(UUID.fromString("f8a2f8d9-4299-4c0c-a88c-8f28b542d42b"))
                .eventIds(new UUID[]{ UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d") })
                .prevProcessingId(UUID.fromString("5ebf5469-6459-471d-93f7-e6a09702fabf"))
                .stepName("ExtensionsBasedEventConvertor")
                .stepIndex(1)
                .timeProcessingStart(Timestamp.valueOf("2021-08-24 09:37:39.411000").toInstant())
                .timeProcessingEnd(Timestamp.valueOf("2021-08-24 09:37:39.441000").toInstant())
                .stepDurationMs(30)
                .payloadJsonStart("{\"id\":\"e2c59478-6032-4bde-a8c1-0ce42248484d\",\"flowId\":\"default-flow\",\"payloadType\":null,\"payloadJson\":{\"@class\":\"com.obj.nc.flows.inpuEventRouting.DummyExtensionBasedEventConverterTests$DummyEventPayload\",\"intField\":15,\"stringField\":\"simple string\"},\"externalId\":null,\"timeCreated\":{\"epochSecond\":1629797858,\"nano\":413000000},\"timeConsumed\":{\"epochSecond\":1629797859,\"nano\":366000000}}")
                .payloadJsonEnd("{\"type\":\"INTENT\",\"attributes\":{},\"id\":\"ee0ee80a-bde8-4bc1-91a2-0abc1cfa39a9\",\"timeCreated\":null,\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\",\"version\":null,\"prevProcessingId\":\"5ebf5469-6459-471d-93f7-e6a09702fabf\",\"stepName\":\"ExtensionsBasedEventConvertor\",\"stepIndex\":1,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":411000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":441000000},\"stepDurationMs\":30,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\"},\"flow-id\":\"default-flow\"},\"body\":{\"@type\":\"CONSTANT_INTENT_CONTENT\",\"attributes\":{},\"body\":\"Text\",\"subject\":\"Subject\",\"attachments\":[],\"contentType\":\"text/plain\",\"contentTypeName\":\"CONSTANT_INTENT_CONTENT\"},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"3d02890b-1b53-465c-b8c3-b0a722483a45\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.doe@objectify.sk\",\"endpointId\":\"john.doe@objectify.sk\"},{\"@type\":\"EMAIL\",\"id\":\"d2ca2a68-1dc4-4052-a93a-67319449ef5b\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.dudly@objectify.sk\",\"endpointId\":\"john.dudly@objectify.sk\"},{\"@type\":\"SMS\",\"id\":\"820023cf-7dd0-407f-abfa-0d6453bde6d3\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"phone\":\"0918111111\",\"endpointId\":\"0918111111\"}]}")
                .version(0)
                .build();
        processingInfoRepository.save(processingInfo);
    
        processingInfo = ProcessingInfo.builder()
                .processingId(UUID.fromString("bec5ccb3-b63b-4447-847b-f9317f0bcc51"))
                .eventIds(new UUID[]{ UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d") })
                .prevProcessingId(UUID.fromString("f8a2f8d9-4299-4c0c-a88c-8f28b542d42b"))
                .stepName("GenerateMessagesFromIntent")
                .stepIndex(2)
                .timeProcessingStart(Timestamp.valueOf("2021-08-24 09:37:39.502000").toInstant())
                .timeProcessingEnd(Timestamp.valueOf("2021-08-24 09:37:39.506000").toInstant())
                .stepDurationMs(4)
                .payloadJsonStart("{\"type\":\"INTENT\",\"attributes\":{},\"id\":\"ee0ee80a-bde8-4bc1-91a2-0abc1cfa39a9\",\"timeCreated\":{\"epochSecond\":1629797859,\"nano\":490000000},\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\",\"version\":0,\"prevProcessingId\":\"5ebf5469-6459-471d-93f7-e6a09702fabf\",\"stepName\":\"ExtensionsBasedEventConvertor\",\"stepIndex\":1,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":411000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":441000000},\"stepDurationMs\":30,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\"},\"flow-id\":\"default-flow\"},\"body\":{\"@type\":\"CONSTANT_INTENT_CONTENT\",\"attributes\":{},\"body\":\"Text\",\"subject\":\"Subject\",\"attachments\":[],\"contentType\":\"text/plain\",\"contentTypeName\":\"CONSTANT_INTENT_CONTENT\"},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"3d02890b-1b53-465c-b8c3-b0a722483a45\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.doe@objectify.sk\",\"endpointId\":\"john.doe@objectify.sk\"},{\"@type\":\"EMAIL\",\"id\":\"d2ca2a68-1dc4-4052-a93a-67319449ef5b\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.dudly@objectify.sk\",\"endpointId\":\"john.dudly@objectify.sk\"},{\"@type\":\"SMS\",\"id\":\"820023cf-7dd0-407f-abfa-0d6453bde6d3\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"phone\":\"0918111111\",\"endpointId\":\"0918111111\"}]}")
                .payloadJsonEnd("{\"type\":\"EMAIL_MESSAGE\",\"attributes\":{},\"id\":\"28ef1473-425b-40a7-a19c-890621303682\",\"timeCreated\":null,\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"bec5ccb3-b63b-4447-847b-f9317f0bcc51\",\"version\":null,\"prevProcessingId\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\",\"stepName\":\"GenerateMessagesFromIntent\",\"stepIndex\":2,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":502000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":506000000},\"stepDurationMs\":4,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"bec5ccb3-b63b-4447-847b-f9317f0bcc51\"},\"flow-id\":\"default-flow\"},\"body\":{\"@class\":\"com.obj.nc.domain.content.email.EmailContent\",\"attributes\":{},\"subject\":\"Subject\",\"text\":\"Text\",\"contentType\":\"text/plain\",\"attachments\":[]},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"3d02890b-1b53-465c-b8c3-b0a722483a45\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.doe@objectify.sk\",\"endpointId\":\"john.doe@objectify.sk\"}]}")
                .version(0)
                .build();
        processingInfoRepository.save(processingInfo);
    
        processingInfo = ProcessingInfo.builder()
                .processingId(UUID.fromString("32b4f74e-9fed-4319-b817-8c9fc929cda8"))
                .eventIds(new UUID[]{ UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d") })
                .prevProcessingId(UUID.fromString("f8a2f8d9-4299-4c0c-a88c-8f28b542d42b"))
                .stepName("GenerateMessagesFromIntent")
                .stepIndex(2)
                .timeProcessingStart(Timestamp.valueOf("2021-08-24 09:37:39.502000").toInstant())
                .timeProcessingEnd(Timestamp.valueOf("2021-08-24 09:37:39.509000").toInstant())
                .stepDurationMs(7)
                .payloadJsonStart("{\"type\":\"INTENT\",\"attributes\":{},\"id\":\"ee0ee80a-bde8-4bc1-91a2-0abc1cfa39a9\",\"timeCreated\":{\"epochSecond\":1629797859,\"nano\":490000000},\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\",\"version\":0,\"prevProcessingId\":\"5ebf5469-6459-471d-93f7-e6a09702fabf\",\"stepName\":\"ExtensionsBasedEventConvertor\",\"stepIndex\":1,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":411000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":441000000},\"stepDurationMs\":30,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\"},\"flow-id\":\"default-flow\"},\"body\":{\"@type\":\"CONSTANT_INTENT_CONTENT\",\"attributes\":{},\"body\":\"Text\",\"subject\":\"Subject\",\"attachments\":[],\"contentType\":\"text/plain\",\"contentTypeName\":\"CONSTANT_INTENT_CONTENT\"},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"3d02890b-1b53-465c-b8c3-b0a722483a45\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.doe@objectify.sk\",\"endpointId\":\"john.doe@objectify.sk\"},{\"@type\":\"EMAIL\",\"id\":\"d2ca2a68-1dc4-4052-a93a-67319449ef5b\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.dudly@objectify.sk\",\"endpointId\":\"john.dudly@objectify.sk\"},{\"@type\":\"SMS\",\"id\":\"820023cf-7dd0-407f-abfa-0d6453bde6d3\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"phone\":\"0918111111\",\"endpointId\":\"0918111111\"}]}")
                .payloadJsonEnd("{\"type\":\"EMAIL_MESSAGE\",\"attributes\":{},\"id\":\"c61d2088-1253-4673-88cc-8c9e084e1432\",\"timeCreated\":null,\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"32b4f74e-9fed-4319-b817-8c9fc929cda8\",\"version\":null,\"prevProcessingId\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\",\"stepName\":\"GenerateMessagesFromIntent\",\"stepIndex\":2,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":502000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":509000000},\"stepDurationMs\":7,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"32b4f74e-9fed-4319-b817-8c9fc929cda8\"},\"flow-id\":\"default-flow\"},\"body\":{\"@class\":\"com.obj.nc.domain.content.email.EmailContent\",\"attributes\":{},\"subject\":\"Subject\",\"text\":\"Text\",\"contentType\":\"text/plain\",\"attachments\":[]},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"d2ca2a68-1dc4-4052-a93a-67319449ef5b\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.dudly@objectify.sk\",\"endpointId\":\"john.dudly@objectify.sk\"}]}")
                .version(0)
                .build();
        processingInfoRepository.save(processingInfo);
    
        processingInfo = ProcessingInfo.builder()
                .processingId(UUID.fromString("b7cdbb43-20f0-4870-abd7-7449e1d600f0"))
                .eventIds(new UUID[]{ UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d") })
                .prevProcessingId(UUID.fromString("e353d9c7-bbf4-46a5-92c9-fc2f12d4843f"))
                .stepName("SendEmail")
                .stepIndex(4)
                .timeProcessingStart(Timestamp.valueOf("2021-08-24 09:37:39.607000").toInstant())
                .timeProcessingEnd(Timestamp.valueOf("2021-08-24 09:37:39.713000").toInstant())
                .stepDurationMs(106)
                .payloadJsonStart("{\"type\":\"EMAIL_MESSAGE\",\"attributes\":{},\"id\":\"4e91137e-93aa-4633-b137-3050442d26a5\",\"timeCreated\":{\"epochSecond\":1629797859,\"nano\":575000000},\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"e353d9c7-bbf4-46a5-92c9-fc2f12d4843f\",\"version\":0,\"prevProcessingId\":\"32b4f74e-9fed-4319-b817-8c9fc929cda8\",\"stepName\":\"MessageByRecipientTokenizer\",\"stepIndex\":3,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":556000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":557000000},\"stepDurationMs\":1,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"e353d9c7-bbf4-46a5-92c9-fc2f12d4843f\"},\"flow-id\":\"default-flow\"},\"body\":{\"@class\":\"com.obj.nc.domain.content.email.EmailContent\",\"attributes\":{},\"subject\":\"Subject\",\"text\":\"Text\",\"contentType\":\"text/plain\",\"attachments\":[]},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"d2ca2a68-1dc4-4052-a93a-67319449ef5b\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.dudly@objectify.sk\",\"endpointId\":\"john.dudly@objectify.sk\"}]}")
                .payloadJsonEnd("{\"type\":\"EMAIL_MESSAGE\",\"attributes\":{},\"id\":\"4e91137e-93aa-4633-b137-3050442d26a5\",\"timeCreated\":{\"epochSecond\":1629797859,\"nano\":575000000},\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"b7cdbb43-20f0-4870-abd7-7449e1d600f0\",\"version\":null,\"prevProcessingId\":\"e353d9c7-bbf4-46a5-92c9-fc2f12d4843f\",\"stepName\":\"SendEmail\",\"stepIndex\":4,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":607000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":713000000},\"stepDurationMs\":106,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"b7cdbb43-20f0-4870-abd7-7449e1d600f0\"},\"flow-id\":\"default-flow\"},\"body\":{\"@class\":\"com.obj.nc.domain.content.email.EmailContent\",\"attributes\":{},\"subject\":\"Subject\",\"text\":\"Text\",\"contentType\":\"text/plain\",\"attachments\":[]},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"d2ca2a68-1dc4-4052-a93a-67319449ef5b\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.dudly@objectify.sk\",\"endpointId\":\"john.dudly@objectify.sk\"}]}")
                .version(0)
                .build();
        processingInfoRepository.save(processingInfo);
    
        processingInfo = ProcessingInfo.builder()
                .processingId(UUID.fromString("8357b163-f148-4b05-aa23-8a78dd12116f"))
                .eventIds(new UUID[]{ UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d") })
                .prevProcessingId(UUID.fromString("f8a2f8d9-4299-4c0c-a88c-8f28b542d42b"))
                .stepName("GenerateMessagesFromIntent")
                .stepIndex(2)
                .timeProcessingStart(Timestamp.valueOf("2021-08-24 09:37:39.502000").toInstant())
                .timeProcessingEnd(Timestamp.valueOf("2021-08-24 09:37:39.510000").toInstant())
                .stepDurationMs(8)
                .payloadJsonStart("{\"type\":\"INTENT\",\"attributes\":{},\"id\":\"ee0ee80a-bde8-4bc1-91a2-0abc1cfa39a9\",\"timeCreated\":{\"epochSecond\":1629797859,\"nano\":490000000},\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\",\"version\":0,\"prevProcessingId\":\"5ebf5469-6459-471d-93f7-e6a09702fabf\",\"stepName\":\"ExtensionsBasedEventConvertor\",\"stepIndex\":1,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":411000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":441000000},\"stepDurationMs\":30,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\"},\"flow-id\":\"default-flow\"},\"body\":{\"@type\":\"CONSTANT_INTENT_CONTENT\",\"attributes\":{},\"body\":\"Text\",\"subject\":\"Subject\",\"attachments\":[],\"contentType\":\"text/plain\",\"contentTypeName\":\"CONSTANT_INTENT_CONTENT\"},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"3d02890b-1b53-465c-b8c3-b0a722483a45\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.doe@objectify.sk\",\"endpointId\":\"john.doe@objectify.sk\"},{\"@type\":\"EMAIL\",\"id\":\"d2ca2a68-1dc4-4052-a93a-67319449ef5b\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.dudly@objectify.sk\",\"endpointId\":\"john.dudly@objectify.sk\"},{\"@type\":\"SMS\",\"id\":\"820023cf-7dd0-407f-abfa-0d6453bde6d3\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"phone\":\"0918111111\",\"endpointId\":\"0918111111\"}]}")
                .payloadJsonEnd("{\"type\":\"SMS_MESSAGE\",\"attributes\":{},\"id\":\"506c5860-96f2-4558-9915-325efaad211a\",\"timeCreated\":null,\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"8357b163-f148-4b05-aa23-8a78dd12116f\",\"version\":null,\"prevProcessingId\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\",\"stepName\":\"GenerateMessagesFromIntent\",\"stepIndex\":2,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":502000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":510000000},\"stepDurationMs\":8,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"8357b163-f148-4b05-aa23-8a78dd12116f\"},\"flow-id\":\"default-flow\"},\"body\":{\"@class\":\"com.obj.nc.domain.content.sms.SimpleTextContent\",\"attributes\":{},\"text\":\"Text\"},\"receivingEndpoints\":[{\"@type\":\"SMS\",\"id\":\"820023cf-7dd0-407f-abfa-0d6453bde6d3\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"phone\":\"0918111111\",\"endpointId\":\"0918111111\"}]}")
                .version(0)
                .build();
        processingInfoRepository.save(processingInfo);
    
        processingInfo = ProcessingInfo.builder()
                .processingId(UUID.fromString("932dc60e-b20b-4a59-a1ec-146c2af82070"))
                .eventIds(new UUID[]{ UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d") })
                .prevProcessingId(UUID.fromString("bec5ccb3-b63b-4447-847b-f9317f0bcc51"))
                .stepName("MessageByRecipientTokenizer")
                .stepIndex(3)
                .timeProcessingStart(Timestamp.valueOf("2021-08-24 09:37:39.515000").toInstant())
                .timeProcessingEnd(Timestamp.valueOf("2021-08-24 09:37:39.518000").toInstant())
                .stepDurationMs(3)
                .payloadJsonStart("{\"type\":\"EMAIL_MESSAGE\",\"attributes\":{},\"id\":\"28ef1473-425b-40a7-a19c-890621303682\",\"timeCreated\":null,\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"bec5ccb3-b63b-4447-847b-f9317f0bcc51\",\"version\":null,\"prevProcessingId\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\",\"stepName\":\"GenerateMessagesFromIntent\",\"stepIndex\":2,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":502000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":506000000},\"stepDurationMs\":4,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"bec5ccb3-b63b-4447-847b-f9317f0bcc51\"},\"flow-id\":\"default-flow\"},\"body\":{\"@class\":\"com.obj.nc.domain.content.email.EmailContent\",\"attributes\":{},\"subject\":\"Subject\",\"text\":\"Text\",\"contentType\":\"text/plain\",\"attachments\":[]},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"3d02890b-1b53-465c-b8c3-b0a722483a45\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.doe@objectify.sk\",\"endpointId\":\"john.doe@objectify.sk\"}]}")
                .payloadJsonEnd("{\"type\":\"EMAIL_MESSAGE\",\"attributes\":{},\"id\":\"5f74929b-2635-4515-9f30-ba79e200e92b\",\"timeCreated\":null,\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"932dc60e-b20b-4a59-a1ec-146c2af82070\",\"version\":null,\"prevProcessingId\":\"bec5ccb3-b63b-4447-847b-f9317f0bcc51\",\"stepName\":\"MessageByRecipientTokenizer\",\"stepIndex\":3,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":515000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":518000000},\"stepDurationMs\":3,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"932dc60e-b20b-4a59-a1ec-146c2af82070\"},\"flow-id\":\"default-flow\"},\"body\":{\"@class\":\"com.obj.nc.domain.content.email.EmailContent\",\"attributes\":{},\"subject\":\"Subject\",\"text\":\"Text\",\"contentType\":\"text/plain\",\"attachments\":[]},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"3d02890b-1b53-465c-b8c3-b0a722483a45\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.doe@objectify.sk\",\"endpointId\":\"john.doe@objectify.sk\"}]}")
                .version(0)
                .build();
        processingInfoRepository.save(processingInfo);
    
        processingInfo = ProcessingInfo.builder()
                .processingId(UUID.fromString("e353d9c7-bbf4-46a5-92c9-fc2f12d4843f"))
                .eventIds(new UUID[]{ UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d") })
                .prevProcessingId(UUID.fromString("32b4f74e-9fed-4319-b817-8c9fc929cda8"))
                .stepName("MessageByRecipientTokenizer")
                .stepIndex(3)
                .timeProcessingStart(Timestamp.valueOf("2021-08-24 09:37:39.556000").toInstant())
                .timeProcessingEnd(Timestamp.valueOf("2021-08-24 09:37:39.557000").toInstant())
                .stepDurationMs(1)
                .payloadJsonStart("{\"type\":\"EMAIL_MESSAGE\",\"attributes\":{},\"id\":\"c61d2088-1253-4673-88cc-8c9e084e1432\",\"timeCreated\":null,\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"32b4f74e-9fed-4319-b817-8c9fc929cda8\",\"version\":0,\"prevProcessingId\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\",\"stepName\":\"GenerateMessagesFromIntent\",\"stepIndex\":2,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":502000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":509000000},\"stepDurationMs\":7,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"32b4f74e-9fed-4319-b817-8c9fc929cda8\"},\"flow-id\":\"default-flow\"},\"body\":{\"@class\":\"com.obj.nc.domain.content.email.EmailContent\",\"attributes\":{},\"subject\":\"Subject\",\"text\":\"Text\",\"contentType\":\"text/plain\",\"attachments\":[]},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"d2ca2a68-1dc4-4052-a93a-67319449ef5b\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.dudly@objectify.sk\",\"endpointId\":\"john.dudly@objectify.sk\"}]}")
                .payloadJsonEnd("{\"type\":\"EMAIL_MESSAGE\",\"attributes\":{},\"id\":\"4e91137e-93aa-4633-b137-3050442d26a5\",\"timeCreated\":null,\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"e353d9c7-bbf4-46a5-92c9-fc2f12d4843f\",\"version\":null,\"prevProcessingId\":\"32b4f74e-9fed-4319-b817-8c9fc929cda8\",\"stepName\":\"MessageByRecipientTokenizer\",\"stepIndex\":3,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":556000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":557000000},\"stepDurationMs\":1,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"e353d9c7-bbf4-46a5-92c9-fc2f12d4843f\"},\"flow-id\":\"default-flow\"},\"body\":{\"@class\":\"com.obj.nc.domain.content.email.EmailContent\",\"attributes\":{},\"subject\":\"Subject\",\"text\":\"Text\",\"contentType\":\"text/plain\",\"attachments\":[]},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"d2ca2a68-1dc4-4052-a93a-67319449ef5b\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.dudly@objectify.sk\",\"endpointId\":\"john.dudly@objectify.sk\"}]}")
                .version(0)
                .build();
        processingInfoRepository.save(processingInfo);
    
        processingInfo = ProcessingInfo.builder()
                .processingId(UUID.fromString("8d70421d-e984-4a75-833c-5a06050af16f"))
                .eventIds(new UUID[]{ UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d") })
                .prevProcessingId(UUID.fromString("8357b163-f148-4b05-aa23-8a78dd12116f"))
                .stepName("MessageByRecipientTokenizer")
                .stepIndex(3)
                .timeProcessingStart(Timestamp.valueOf("2021-08-24 09:37:39.583000").toInstant())
                .timeProcessingEnd(Timestamp.valueOf("2021-08-24 09:37:39.583000").toInstant())
                .stepDurationMs(0)
                .payloadJsonStart("{\"type\":\"SMS_MESSAGE\",\"attributes\":{},\"id\":\"506c5860-96f2-4558-9915-325efaad211a\",\"timeCreated\":null,\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"8357b163-f148-4b05-aa23-8a78dd12116f\",\"version\":0,\"prevProcessingId\":\"f8a2f8d9-4299-4c0c-a88c-8f28b542d42b\",\"stepName\":\"GenerateMessagesFromIntent\",\"stepIndex\":2,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":502000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":510000000},\"stepDurationMs\":8,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"8357b163-f148-4b05-aa23-8a78dd12116f\"},\"flow-id\":\"default-flow\"},\"body\":{\"@class\":\"com.obj.nc.domain.content.sms.SimpleTextContent\",\"attributes\":{},\"text\":\"Text\"},\"receivingEndpoints\":[{\"@type\":\"SMS\",\"id\":\"820023cf-7dd0-407f-abfa-0d6453bde6d3\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"phone\":\"0918111111\",\"endpointId\":\"0918111111\"}]}")
                .payloadJsonEnd("{\"type\":\"SMS_MESSAGE\",\"attributes\":{},\"id\":\"c3ce484f-f03c-47b1-ae6a-2337ca4f6444\",\"timeCreated\":null,\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"8d70421d-e984-4a75-833c-5a06050af16f\",\"version\":null,\"prevProcessingId\":\"8357b163-f148-4b05-aa23-8a78dd12116f\",\"stepName\":\"MessageByRecipientTokenizer\",\"stepIndex\":3,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":583000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":583000000},\"stepDurationMs\":0,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"8d70421d-e984-4a75-833c-5a06050af16f\"},\"flow-id\":\"default-flow\"},\"body\":{\"@class\":\"com.obj.nc.domain.content.sms.SimpleTextContent\",\"attributes\":{},\"text\":\"Text\"},\"receivingEndpoints\":[{\"@type\":\"SMS\",\"id\":\"820023cf-7dd0-407f-abfa-0d6453bde6d3\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"phone\":\"0918111111\",\"endpointId\":\"0918111111\"}]}")
                .version(0)
                .build();
        processingInfoRepository.save(processingInfo);
    
        processingInfo = ProcessingInfo.builder()
                .processingId(UUID.fromString("370c0713-ae71-45e4-9a51-2abe7b5b9af8"))
                .eventIds(new UUID[]{ UUID.fromString("e2c59478-6032-4bde-a8c1-0ce42248484d") })
                .prevProcessingId(UUID.fromString("932dc60e-b20b-4a59-a1ec-146c2af82070"))
                .stepName("SendEmail")
                .stepIndex(4)
                .timeProcessingStart(Timestamp.valueOf("2021-08-24 09:37:39.580000").toInstant())
                .timeProcessingEnd(Timestamp.valueOf("2021-08-24 09:37:39.714000").toInstant())
                .stepDurationMs(134)
                .payloadJsonStart("{\"type\":\"EMAIL_MESSAGE\",\"attributes\":{},\"id\":\"5f74929b-2635-4515-9f30-ba79e200e92b\",\"timeCreated\":{\"epochSecond\":1629797859,\"nano\":540000000},\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"932dc60e-b20b-4a59-a1ec-146c2af82070\",\"version\":0,\"prevProcessingId\":\"bec5ccb3-b63b-4447-847b-f9317f0bcc51\",\"stepName\":\"MessageByRecipientTokenizer\",\"stepIndex\":3,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":515000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":518000000},\"stepDurationMs\":3,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"932dc60e-b20b-4a59-a1ec-146c2af82070\"},\"flow-id\":\"default-flow\"},\"body\":{\"@class\":\"com.obj.nc.domain.content.email.EmailContent\",\"attributes\":{},\"subject\":\"Subject\",\"text\":\"Text\",\"contentType\":\"text/plain\",\"attachments\":[]},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"3d02890b-1b53-465c-b8c3-b0a722483a45\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.doe@objectify.sk\",\"endpointId\":\"john.doe@objectify.sk\"}]}")
                .payloadJsonEnd("{\"type\":\"EMAIL_MESSAGE\",\"attributes\":{},\"id\":\"5f74929b-2635-4515-9f30-ba79e200e92b\",\"timeCreated\":{\"epochSecond\":1629797859,\"nano\":540000000},\"header\":{\"attributes\":{},\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"processingInfo\":{\"processingId\":\"370c0713-ae71-45e4-9a51-2abe7b5b9af8\",\"version\":null,\"prevProcessingId\":\"932dc60e-b20b-4a59-a1ec-146c2af82070\",\"stepName\":\"SendEmail\",\"stepIndex\":4,\"timeProcessingStart\":{\"epochSecond\":1629797859,\"nano\":580000000},\"timeProcessingEnd\":{\"epochSecond\":1629797859,\"nano\":714000000},\"stepDurationMs\":134,\"eventIds\":[\"e2c59478-6032-4bde-a8c1-0ce42248484d\"],\"new\":true,\"id\":\"370c0713-ae71-45e4-9a51-2abe7b5b9af8\"},\"flow-id\":\"default-flow\"},\"body\":{\"@class\":\"com.obj.nc.domain.content.email.EmailContent\",\"attributes\":{},\"subject\":\"Subject\",\"text\":\"Text\",\"contentType\":\"text/plain\",\"attachments\":[]},\"receivingEndpoints\":[{\"@type\":\"EMAIL\",\"id\":\"3d02890b-1b53-465c-b8c3-b0a722483a45\",\"deliveryOptions\":null,\"recipient\":null,\"timeCreated\":null,\"email\":\"john.doe@objectify.sk\",\"endpointId\":\"john.doe@objectify.sk\"}]}")
                .version(0)
                .build();
        processingInfoRepository.save(processingInfo);
    }
    
    public Instant randomInstantBetween(Instant startInclusive, Instant endExclusive) {
        long startSeconds = startInclusive.getEpochSecond();
        long endSeconds = endExclusive.getEpochSecond();
        long random = ThreadLocalRandom
                .current()
                .nextLong(startSeconds, endSeconds);
        return Instant.ofEpochSecond(random);
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    static class DummyEventPayload {
    
        String stringField;
        Long longField;
        
        JsonNode toJsonNode() {
            return JsonUtils.readJsonNodeFromPojo(this);
        }
        
    }
    
}
