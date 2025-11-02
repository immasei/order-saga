package com.example.store.kafka.event;

import com.example.store.kafka.command.ChargePayment;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PaymentSucceeded(
    String orderNumber,
    String idempotencyKey,
//    PaymentResponseDTO payment,
    LocalDateTime createdAt
){
    public static PaymentSucceeded of(ChargePayment cmd) {
        return PaymentSucceeded.builder()
                .orderNumber(cmd.orderNumber())
                .idempotencyKey(cmd.idempotencyKey())
                .createdAt(LocalDateTime.now())
                .build();
    }

}
