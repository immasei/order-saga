package com.example.emailservice.messaging.dto;


import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;



public record DeliveryStatusMessage(
        UUID eventId,
        UUID deliveryOrderId,
        String externalOrderId,
        UUID correlationId,
        String status,
        String reason,

        OffsetDateTime occurredAt,
        Map<String, Object> payload
) {}


