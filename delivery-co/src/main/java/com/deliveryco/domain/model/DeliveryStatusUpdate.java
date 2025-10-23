package com.deliveryco.domain.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record DeliveryStatusUpdate(
        UUID eventId,
        String externalOrderId,
        DeliveryOrderStatus status,
        OffsetDateTime occurredAt,
        String reason,
        Map<String, Object> payload
) {
}

