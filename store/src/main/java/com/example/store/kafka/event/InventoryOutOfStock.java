package com.example.store.kafka.event;

import com.example.store.exception.InsufficientStockException;

import java.time.LocalDateTime;
import java.util.List;

public record InventoryOutOfStock(
        String orderNumber,
        String idempotencyKey,
        List<MissingItem> missingItems,
        String reason,
        LocalDateTime detectedAt
) {
    public record MissingItem(
            String productCode,
            int required,
            int available
    ) {}

    public static InventoryOutOfStock of(String orderNumber,
                                         String idempotencyKey,
                                         List<InsufficientStockException.MissingItem> missing,
                                         String reason) {
        List<MissingItem> mapped = missing.stream()
                .map(m -> new MissingItem(m.productCode(), m.required(), m.available()))
                .toList();

        return new InventoryOutOfStock(orderNumber, idempotencyKey, mapped, reason, LocalDateTime.now());
    }
}
