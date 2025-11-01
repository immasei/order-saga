package com.example.store.kafka.command;

import com.example.store.kafka.event.PaymentSucceeded;
import com.example.store.model.Order;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record CreateShipment (
    String orderNumber,
    String idempotencyKey,
    String customerId,
    String customerEmail,
    String dropOffAddress,
    Map<String, String> pickupLocations,
    Map<String, Map<String, Integer>> productsByWarehouse,
    LocalDateTime createdAt
) {
    public static CreateShipment of(
            Order order,
            PaymentSucceeded evt,
            Map<String, String> pickupLocations,
            Map<String, Map<String, Integer>> productsByWarehouse
    ) {
        return CreateShipment.builder()
                .orderNumber(evt.orderNumber())
                .idempotencyKey(evt.idempotencyKey())
                .customerId(order.getCustomer().getId().toString())
                .customerEmail(order.getCustomer().getEmail())
                .dropOffAddress(order.getDeliveryAddress())
                .pickupLocations(pickupLocations)
                .productsByWarehouse(productsByWarehouse)
                .createdAt(LocalDateTime.now())
                .build();
    }
}

