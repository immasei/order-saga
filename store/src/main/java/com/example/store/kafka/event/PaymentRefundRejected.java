package com.example.store.kafka.event;

import com.example.store.enums.EventType;
import com.example.store.enums.RefundOutcome;
import com.example.store.kafka.command.RefundPayment;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PaymentRefundRejected (
        String orderNumber,
        String idempotencyKey,
        EventType triggerBy,    // root cause: shipment_failed/ cancelled by user
        RefundOutcome outcome,  // rejected
        EventType reason,       // order shipped
        LocalDateTime rejectedAt
) {
    public static PaymentRefundRejected of(RefundPayment evt) {
        return PaymentRefundRejected.builder()
                .orderNumber(evt.orderNumber())
                .idempotencyKey(evt.idempotencyKey())
                .triggerBy(evt.triggerBy()) // payment_fail
                .outcome(RefundOutcome.REJECTED_NOT_ALLOWED)
                .reason(EventType.ORDER_SHIPPED)
                .rejectedAt(LocalDateTime.now())
                .build();
    }
}
