package com.example.store.kafka.event;

import com.example.store.dto.bank.PaymentResponseDTO;
import com.example.store.enums.EventType;
import com.example.store.kafka.command.ChargePayment;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PaymentFailed(
        String orderNumber,
        String idempotencyKey,
        EventType reason,
        LocalDateTime createdAt
){
    public static PaymentFailed of(ChargePayment cmd) {
        return PaymentFailed.builder()
                .orderNumber(cmd.orderNumber())
                .idempotencyKey(cmd.idempotencyKey())
                .reason(EventType.PAYMENT_FAILED)
                .createdAt(LocalDateTime.now())
                .build();
    }

}
