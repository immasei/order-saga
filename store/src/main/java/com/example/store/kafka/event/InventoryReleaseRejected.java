package com.example.store.kafka.event;

import com.example.store.enums.EventType;
import com.example.store.enums.RefundOutcome;
import com.example.store.enums.ReleaseOutcome;
import com.example.store.kafka.command.ReleaseInventory;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InventoryReleaseRejected (
    String orderNumber,
    String idempotencyKey,
    EventType triggerBy,           // payment_failed/ cancelled by user
    ReleaseOutcome releaseOutcome, // rejected
    EventType releaseOutcomeCause, // order_shipped
    RefundOutcome refundOutcome,   // can be null if refund not attempted
    EventType refundOutcomeCause,
    boolean isCancellable,
    LocalDateTime rejectedAt
) {
    public static InventoryReleaseRejected of(ReleaseInventory cmd) {
        return InventoryReleaseRejected.builder()
                .orderNumber(cmd.orderNumber())
                .idempotencyKey(cmd.idempotencyKey())
                .triggerBy(cmd.triggerBy())
                .releaseOutcome(ReleaseOutcome.REJECTED_NOT_ALLOWED)
                .releaseOutcomeCause(EventType.ORDER_SHIPPED) // aka inventory committed
                .refundOutcome(null)
                .refundOutcomeCause(null)
                .isCancellable(false)
                .rejectedAt(LocalDateTime.now())
                .build();
    }
}
