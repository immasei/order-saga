package com.example.store.kafka.command;

import java.time.LocalDateTime;

public record ReleaseInventory(
        String orderNumber,
        String idempotencyKey,
        String reason,
        LocalDateTime createdAt
) {
    public static ReleaseInventory of(String orderNumber, String idempotencyKey, String reason) {
        return new ReleaseInventory(orderNumber, idempotencyKey, reason, LocalDateTime.now());
    }
}
