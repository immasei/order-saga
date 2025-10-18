package com.deliveryco.web.dto;

import com.deliveryco.domain.model.DeliveryOrderStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DeliveryResponseDto(
        UUID deliveryOrderId,
        String externalOrderId,
        DeliveryOrderStatus status,
        OffsetDateTime requestedAt
) {
}

