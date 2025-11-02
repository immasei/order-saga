package com.example.store.kafka.command;

import com.example.store.enums.EventType;
import com.example.store.enums.RefundOutcome;
import com.example.store.kafka.event.PaymentFailed;
import com.example.store.kafka.event.PaymentRefundRejected;
import com.example.store.kafka.event.PaymentRefunded;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReleaseInventory(
        String orderNumber,
        String idempotencyKey,
        EventType triggerBy, // root cause : payment_failed/ cancelled by user/ shipment_failed
        RefundOutcome refundOutcome, // can be null if refund not attempted
        EventType refundOutcomeCause,
        LocalDateTime createdAt
) {
    public static ReleaseInventory of(PaymentFailed evt) {
        return ReleaseInventory.builder()
                .orderNumber(evt.orderNumber())
                .idempotencyKey(evt.idempotencyKey())
                .triggerBy(evt.reason()) // payment_failed
                .refundOutcome(null)
                .refundOutcomeCause(null)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ReleaseInventory of(PaymentRefundRejected evt) {
        return ReleaseInventory.builder()
                .orderNumber(evt.orderNumber())
                .idempotencyKey(evt.idempotencyKey())
                .triggerBy(evt.triggerBy()) // shipment_failed/cancelled by user
                .refundOutcome(evt.outcome())
                .refundOutcomeCause(evt.reason())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ReleaseInventory of(PaymentRefunded evt) {
        return ReleaseInventory.builder()
                .orderNumber(evt.orderNumber())
                .idempotencyKey(evt.idempotencyKey())
                .triggerBy(evt.triggerBy()) // shipment_failed/cancelled by user
                .refundOutcome(evt.outcome())
                .refundOutcomeCause(evt.reason())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
