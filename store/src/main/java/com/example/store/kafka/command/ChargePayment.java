package com.example.store.kafka.command;

import com.example.store.kafka.event.InventoryReserved;
import com.example.store.model.Order;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ChargePayment (
    String orderNumber,
    BigDecimal amount,
    String paymentAccountRef,
    String idempotencyKey,
    LocalDateTime createdAt
) {
    public static ChargePayment of(Order order, InventoryReserved evt) {
        return ChargePayment.builder()
                .orderNumber(evt.orderNumber())
                .amount(order.getTotal())
                .paymentAccountRef(order.getPaymentAccountRef())
                .idempotencyKey(evt.idempotencyKey())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
