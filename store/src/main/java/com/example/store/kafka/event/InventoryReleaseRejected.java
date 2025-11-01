package com.example.store.kafka.event;

import com.example.store.enums.EventType;
import com.example.store.enums.ReleaseOutcome;
import com.example.store.kafka.command.ReleaseInventory;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InventoryReleaseRejected (
    String orderNumber,
    String idempotencyKey,
    EventType triggerBy,    // payment_failed/ cancelled by user
    ReleaseOutcome outcome, // rejected
    EventType reason,       //committed
    boolean isCancellable,
    LocalDateTime rejectedAt
) {
    public static InventoryReleaseRejected of(ReleaseInventory cmd) {
        return InventoryReleaseRejected.builder()
                .orderNumber(cmd.orderNumber())
                .idempotencyKey(cmd.idempotencyKey())
                .triggerBy(cmd.reason())
                .outcome(ReleaseOutcome.REJECTED_NOT_ALLOWED)
                .reason(EventType.ORDER_SHIPPED)
                .isCancellable(false)
                .rejectedAt(LocalDateTime.now())
                .build();
    }
}
