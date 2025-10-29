package com.deliveryco.domain.service;

import com.deliveryco.domain.model.DeliveryOrderStatus;
import com.deliveryco.domain.model.OutboxPublishState;
import com.deliveryco.entity.DeliveryOrderEntity;
import com.deliveryco.entity.DeliveryOutboxEntity;
import com.deliveryco.entity.DeliveryStatusEventEntity;
import com.deliveryco.repository.DeliveryOutboxRepository;
import com.deliveryco.repository.DeliveryStatusEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusEventService {

    private final DeliveryStatusEventRepository statusEventRepository;
    private final DeliveryOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public DeliveryStatusEventEntity recordStatus(
            DeliveryOrderEntity order,
            DeliveryOrderStatus status,
            String reason
    ) {
        return recordStatus(order, status, reason, Map.of());
    }

    @Transactional
    public DeliveryStatusEventEntity recordStatus(
            DeliveryOrderEntity order,
            DeliveryOrderStatus status,
            String reason,
            Map<String, Object> payload
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        Map<String, Object> enrichedPayload = new HashMap<>();
        if (order.getContactEmail() != null && !order.getContactEmail().isBlank()) {
            enrichedPayload.put("contactEmail", order.getContactEmail());
        }
        if (payload != null && !payload.isEmpty()) {
            enrichedPayload.putAll(payload);
        }
        UUID eventId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        DeliveryStatusEventEntity event = DeliveryStatusEventEntity.builder()
                .id(eventId)
                .deliveryOrder(order)
                .correlationId(correlationId)
                .status(status)
                .reason(reason)
                .payload(writePayload(enrichedPayload))
                .occurredAt(now)
                .build();
        order.setCurrentStatus(status);
        if (status == DeliveryOrderStatus.PICKED_UP && order.getAcknowledgedAt() == null) {
            order.setAcknowledgedAt(now);
        }
        if ((status == DeliveryOrderStatus.DELIVERED || status == DeliveryOrderStatus.LOST)
                && order.getCompletedAt() == null) {
            order.setCompletedAt(now);
        }

        DeliveryStatusEventEntity savedEvent = statusEventRepository.save(event);
        String messageJson = buildMessageJson(order, savedEvent, enrichedPayload);
        DeliveryOutboxEntity outbox = DeliveryOutboxEntity.builder()
                .id(UUID.randomUUID())
                .deliveryOrder(order)
                .eventType("DELIVERY_STATUS")
                .payload(messageJson)
                .createdAt(now)
                .updatedAt(now)
                .publishState(OutboxPublishState.PENDING)
                .build();
        outboxRepository.save(outbox);
        return savedEvent;
    }

    private String buildMessageJson(
            DeliveryOrderEntity order,
            DeliveryStatusEventEntity event,
            Map<String, Object> payload
    ) {
        var message = new com.deliveryco.messaging.dto.DeliveryStatusMessage(
                event.getId(),
                order.getId(),
                order.getExternalOrderId(),
                event.getCorrelationId(),
                event.getStatus(),
                event.getReason(),
                event.getOccurredAt(),
                payload
        );
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize DeliveryStatusMessage, storing empty payload", e);
            return "{}";
        }
    }

    private String writePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize payload {}, storing empty JSON", payload, e);
            return "{}";
        }
    }
}
