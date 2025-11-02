package com.example.store.kafka.event;

import com.example.store.enums.EventType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderCancellationRejected(
        String orderNumber,
        EventType reason,
        LocalDateTime createdAt
) {
    public static OrderCancellationRejected of(OrderCancellationRequested evt, EventType reason) {
        return OrderCancellationRejected.builder()
                .orderNumber(evt.orderNumber())
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
