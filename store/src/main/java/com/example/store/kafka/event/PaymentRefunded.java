package com.example.store.kafka.event;

import com.example.store.enums.EventType;
import com.example.store.enums.RefundOutcome;
import com.example.store.kafka.command.RefundPayment;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PaymentRefunded (
        String orderNumber,
        String idempotencyKey,
        EventType triggerBy,    // root cause: shipment_failed/ cancelled by user
        RefundOutcome outcome,  // rejected
        EventType reason,       // order shipped
        LocalDateTime rejectedAt
) {
    public static PaymentRefunded refunded(RefundPayment evt, EventType reason) {
        return PaymentRefunded.builder()
                .orderNumber(evt.orderNumber())
                .idempotencyKey(evt.idempotencyKey())
                .triggerBy(evt.triggerBy())
                .outcome(RefundOutcome.SUCCESS)
                .reason(reason)
                .rejectedAt(LocalDateTime.now())
                .build();
    }

    public static PaymentRefunded refunding(RefundPayment evt) {
        return PaymentRefunded.builder()
                .orderNumber(evt.orderNumber())
                .idempotencyKey(evt.idempotencyKey())
                .triggerBy(evt.triggerBy())
                .outcome(RefundOutcome.PROVIDER_ERROR)
                .reason(EventType.BANK_API_TIMEOUT)
                .rejectedAt(LocalDateTime.now())
                .build();
    }

    public static PaymentRefunded noOp(RefundPayment evt) {
        return PaymentRefunded.builder()
                .orderNumber(evt.orderNumber())
                .idempotencyKey(evt.idempotencyKey())
                .triggerBy(evt.triggerBy())
                .outcome(RefundOutcome.NO_ACTION_REQUIRED)
                .reason(EventType.NOTHING_TO_REFUND)
                .rejectedAt(LocalDateTime.now())
                .build();
    }

}