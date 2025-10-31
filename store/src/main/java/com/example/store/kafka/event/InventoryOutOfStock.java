package com.example.store.kafka.event;

import com.example.store.enums.EventType;
import com.example.store.exception.InsufficientStockException;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record InventoryOutOfStock(
    String orderNumber,
    List<InsufficientStockException.MissingItem> missingItems,
    EventType reason,
    LocalDateTime detectedAt
) {
    public static InventoryOutOfStock of(
        String orderNumber, List<InsufficientStockException.MissingItem> missing
    ) {
        return InventoryOutOfStock.builder()
                .orderNumber(orderNumber)
                .missingItems(missing)
                .reason(EventType.INVENTORY_OUT_OF_STOCK)
                .detectedAt(LocalDateTime.now())
                .build();
    }
}
