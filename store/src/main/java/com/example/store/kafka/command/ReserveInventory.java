package com.example.store.kafka.command;

import com.example.store.kafka.event.OrderPlaced;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ReserveInventory (
        String orderNumber,
        List<Item> items,
        String idempotencyKey,
        LocalDateTime createdAt
) {
    public record Item(
            String productCode,
            int quantity
    ) {}

    public static ReserveInventory of(OrderPlaced evt) {
        return ReserveInventory.builder()
            .orderNumber(evt.orderNumber())
            .items(evt.items().stream()
                .map(i -> new Item(i.productCode(), i.quantity()))
                .toList())
            .idempotencyKey(evt.idempotencyKey())
            .createdAt(LocalDateTime.now())
            .build();
    }
}
