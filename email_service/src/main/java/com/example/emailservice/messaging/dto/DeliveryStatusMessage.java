package com.example.emailservice.messaging.dto;

import java.util.Map;
import java.util.UUID;

// Note: occurredAt is sent by DeliveryCo as a numeric epoch (seconds with fractions).
// To avoid deserialization issues, accept it as a Double.
public record DeliveryStatusMessage(
        UUID eventId,
        UUID deliveryOrderId,
        String externalOrderId,
        UUID correlationId,
        String status,
        String reason,
        Double occurredAt,
        Map<String, Object> payload
) {
}
