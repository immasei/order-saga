package com.example.store.dto.delivery;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeliveryStatusCallbackDTO(
        UUID eventId,
        String externalOrderId,
        String status,
        String reason,
        OffsetDateTime occurredAt
) {}