package com.example.store.kafka.event;

import com.example.store.dto.delivery.DeliveryResponseDTO;
import com.example.store.kafka.command.CreateShipment;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ShipmentCreated(
    String orderNumber,
    UUID deliveryTrackingId,
    String idempotencyKey,
    LocalDateTime createdAt
){
    public static ShipmentCreated of(CreateShipment cmd, DeliveryResponseDTO deliveryResponse) {
        return ShipmentCreated.builder()
                .orderNumber(cmd.orderNumber())
                .idempotencyKey(cmd.idempotencyKey())
                .deliveryTrackingId(deliveryResponse.getDeliveryOrderId())
                .createdAt(LocalDateTime.now())
                .build();
    }

}