package com.example.store.kafka.event;

import com.example.store.enums.EventType;
import com.example.store.kafka.command.ReleaseInventory;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InventoryReleased(
        String orderNumber,
        String idempotencyKey,
        EventType reason,
        boolean isCancellable,
        LocalDateTime releasedAt
) {
    public static InventoryReleased of(ReleaseInventory cmd, boolean isCancellable, EventType reason) {
        return InventoryReleased.builder()
                .orderNumber(cmd.orderNumber())
                .idempotencyKey(cmd.idempotencyKey())
                .reason(reason)
                .isCancellable(isCancellable)
                .releasedAt(LocalDateTime.now())
                .build();
    }
}
