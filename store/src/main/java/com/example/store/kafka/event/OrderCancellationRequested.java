package com.example.store.kafka.event;

import com.example.store.enums.EventType;
import com.example.store.model.Order;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderCancellationRequested (
        String orderNumber,
        String idempotencyKey,
        EventType reason,
        LocalDateTime createdAt
) {
    public static OrderCancellationRequested of(Order order) {
        return OrderCancellationRequested.builder()
                .orderNumber(order.getOrderNumber())
                .reason(EventType.CANCELLED_BY_USER)
                .idempotencyKey(order.getIdempotencyKey())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
