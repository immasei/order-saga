package com.example.store.kafka.event;

import com.example.store.enums.EventType;
import com.example.store.model.Order;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DeliveryLost(
        String orderNumber,
        String idempotencyKey,
        EventType reason,
        LocalDateTime createdAt
){
    public static DeliveryLost of(Order order) {
        return DeliveryLost.builder()
                .orderNumber(order.getOrderNumber())
                .idempotencyKey(order.getIdempotencyKey())
                .reason(EventType.LOST_IN_DELIVERY)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
