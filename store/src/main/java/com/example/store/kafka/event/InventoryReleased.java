package com.example.store.kafka.event;

import com.example.store.enums.EventType;
import com.example.store.enums.RefundOutcome;
import com.example.store.enums.ReleaseOutcome;
import com.example.store.kafka.command.ReleaseInventory;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InventoryReleased(
        String orderNumber,
        String idempotencyKey,
        EventType triggerBy,           // root cause: payment_failed/ cancelled by user/ shipment failed
        ReleaseOutcome releaseOutcome, // success/ noop
        EventType releaseOutcomeCause, // order not shipped/ nothing to release
        RefundOutcome refundOutcome,   // can be null if refund not attempted
        EventType refundOutcomeCause,  //
        boolean isCancellable,         // true
        LocalDateTime releasedAt
) {
    public static InventoryReleased released(ReleaseInventory cmd, EventType reason) {
        return InventoryReleased.builder()
                .orderNumber(cmd.orderNumber())
                .idempotencyKey(cmd.idempotencyKey())
                .triggerBy(cmd.triggerBy())
                .releaseOutcome(ReleaseOutcome.SUCCESS)
                .releaseOutcomeCause(reason)
                .refundOutcome(cmd.refundOutcome())
                .refundOutcomeCause(cmd.refundOutcomeCause())
                .isCancellable(true)
                .releasedAt(LocalDateTime.now())
                .build();
    }

    public static InventoryReleased noOp(ReleaseInventory cmd) {
        return InventoryReleased.builder()
                .orderNumber(cmd.orderNumber())
                .idempotencyKey(cmd.idempotencyKey())
                .triggerBy(cmd.triggerBy())
                .releaseOutcome(ReleaseOutcome.NO_ACTION_REQUIRED)
                .releaseOutcomeCause(EventType.NOTHING_TO_RELEASE) // nothing to release
                .refundOutcome(cmd.refundOutcome())
                .refundOutcomeCause(cmd.refundOutcomeCause())
                .isCancellable(true)
                .releasedAt(LocalDateTime.now())
                .build();
    }
}
