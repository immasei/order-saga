package com.example.store.kafka.event;

import com.example.store.dto.order.OrderDTO;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderPlaced(
    String orderNumber,
    String customerEmail,
    List<Item> items,
    String idempotencyKey,
    LocalDateTime createdAt
) {
    public record Item(
        String productCode,
        int quantity
    ) {}

    public static OrderPlaced of(OrderDTO order, String idempotencyKey) {
        return OrderPlaced.builder()
            .orderNumber(order.getOrderNumber())
            .customerEmail(order.getCustomerEmail())
            .items(order.getOrderItems().stream()
                .map(i ->
                    new Item(i.getProductCodeAtPurchase(), i.getQuantity()))
                .toList())
            .idempotencyKey(idempotencyKey)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
