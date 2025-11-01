package com.example.store.kafka.event;

import com.example.store.enums.EventType;
import com.example.store.enums.ReleaseOutcome;
import com.example.store.kafka.command.ReleaseInventory;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InventoryReleased(
        String orderNumber,
        String idempotencyKey,
        EventType triggerBy,    // payment_failed/ cancelled by user
        ReleaseOutcome outcome, // success/ noop
        EventType reason,       // order not shipped/ nothing to release
        boolean isCancellable,  // true
        LocalDateTime releasedAt
) {
    public static InventoryReleased released(ReleaseInventory cmd, EventType reason) {
        return InventoryReleased.builder()
                .orderNumber(cmd.orderNumber())
                .idempotencyKey(cmd.idempotencyKey())
                .triggerBy(cmd.reason())
                .outcome(ReleaseOutcome.SUCCESS)
                .reason(reason)
                .isCancellable(true)
                .releasedAt(LocalDateTime.now())
                .build();
    }

    public static InventoryReleased noOp(ReleaseInventory cmd) {
        return InventoryReleased.builder()
                .orderNumber(cmd.orderNumber())
                .idempotencyKey(cmd.idempotencyKey())
                .triggerBy(cmd.reason())
                .outcome(ReleaseOutcome.NOOP_ORPHAN)
                .reason(EventType.NOTHING_TO_RELEASE) // nothing to release
                .isCancellable(true)
                .releasedAt(LocalDateTime.now())
                .build();
    }
}
