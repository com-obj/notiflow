package com.obj.nc.functions.processors.deliveryInfo;

import com.obj.nc.domain.relationship.Message2EndpointRelation;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.Message2EndpointRelationRepository;
import com.obj.nc.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeliveryInfoStatusRecorder {

    private final DeliveryInfoRepository deliveryInfoRepository;
    private final MessageRepository messageRepository;
    private final Message2EndpointRelationRepository messageToEndpointRepository;

    @Transactional
    public List<DeliveryInfo> persist(List<DeliveryInfo> deliveryInfos) {
        List<DeliveryInfo> persisted = new ArrayList<>();

        for (DeliveryInfo deliveryInfo : deliveryInfos) {
            if (DELIVERY_STATUS.SENT.equals(deliveryInfo.getStatus())
                    && deliveryInfo.getMessageId() != null) {
                messageRepository.lockById(deliveryInfo.getMessageId());
                if (deliveryInfoRepository.hasProviderStatus(deliveryInfo.getMessageId())) {
                    continue;
                }
            }

            if (deliveryInfo.isNew()) {
                deliveryInfo.setId(UUID.randomUUID());
            }
            persisted.add(deliveryInfoRepository.save(deliveryInfo));
        }

        return persisted;
    }

    @Transactional
    public boolean recordProviderStatus(UUID messageId, DELIVERY_STATUS status, String additionalInformation) {
        if (!messageRepository.lockById(messageId).isPresent()) {
            return false;
        }

        List<DeliveryInfo> existingTransitions =
                deliveryInfoRepository.findExactByMessageIdAndStatus(messageId, status);
        if (!existingTransitions.isEmpty()) {
            DeliveryInfo existingTransition = existingTransitions.get(0);
            existingTransition.setAdditionalInformation(additionalInformation);
            deliveryInfoRepository.save(existingTransition);
            return true;
        }

        List<Message2EndpointRelation> endpointRelations = messageToEndpointRepository.findByMessageId(messageId);
        if (endpointRelations.isEmpty()) {
            return false;
        }

        endpointRelations.stream()
                .map(relation -> DeliveryInfo.builder()
                        .id(UUID.randomUUID())
                        .messageId(messageId)
                        .endpointId(relation.getEndpointId())
                        .status(status)
                        .additionalInformation(additionalInformation)
                        .build())
                .forEach(deliveryInfoRepository::save);
        return true;
    }
}
