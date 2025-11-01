package com.example.store.kafka.event;

import com.example.store.enums.EventType;
import com.example.store.kafka.command.CreateShipment;
import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record ShipmentFailed(
        String orderNumber,
        String idempotencyKey,
        EventType reason,
        LocalDateTime createdAt
){
    public static ShipmentFailed of(CreateShipment cmd) {
        return ShipmentFailed.builder()
                .orderNumber(cmd.orderNumber())
                .idempotencyKey(cmd.idempotencyKey())
                .reason(EventType.SHIPPING_FAILED)
                .createdAt(LocalDateTime.now())
                .build();
    }

}
