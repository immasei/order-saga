package com.example.store.dto.delivery;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DeliveryResponseDTO {
    private UUID deliveryOrderId;
    private String externalOrderId;
    private String status;
    private OffsetDateTime requestedAt;
}