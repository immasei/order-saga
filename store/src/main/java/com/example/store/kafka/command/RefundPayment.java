package com.example.store.kafka.command;

import com.example.store.enums.EventType;
import com.example.store.kafka.event.DeliveryLost;
import com.example.store.kafka.event.ShipmentFailed;
import com.example.store.model.Order;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record RefundPayment(
        String orderNumber,
        BigDecimal amount,
        String paymentAccountRef,
        EventType triggerBy, // root cause: shipment_failed/ cancelled by user
        String idempotencyKey,
        LocalDateTime createdAt
) {
    public static RefundPayment of(Order order, ShipmentFailed evt) {
        return RefundPayment.builder()
                .orderNumber(evt.orderNumber())
                .amount(order.getTotal())
                .paymentAccountRef(order.getPaymentAccountRef())
                .idempotencyKey(evt.idempotencyKey())
                .triggerBy(evt.reason()) // shipment_failed
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static RefundPayment of(Order order, DeliveryLost evt) {
        return RefundPayment.builder()
                .orderNumber(evt.orderNumber())
                .amount(order.getTotal())
                .paymentAccountRef(order.getPaymentAccountRef())
                .idempotencyKey(evt.idempotencyKey())
                .triggerBy(evt.reason()) // shipment_failed
                .createdAt(LocalDateTime.now())
                .build();
    }

}