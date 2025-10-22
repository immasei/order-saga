package com.deliveryco.messaging.dto;

import com.deliveryco.domain.model.DeliveryOrderStatus;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record DeliveryStatusMessage(
        UUID eventId,
        UUID deliveryOrderId,
        String externalOrderId,
        UUID correlationId,
        DeliveryOrderStatus status,
        String reason,
        OffsetDateTime occurredAt,
        Map<String, Object> payload
) {
}

