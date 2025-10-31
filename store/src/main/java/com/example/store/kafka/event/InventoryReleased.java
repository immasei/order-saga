package com.example.store.kafka.event;

import com.example.store.dto.inventory.InventoryAllocationDTO;

import java.time.LocalDateTime;
import java.util.List;

public record InventoryReleased(
        String orderNumber,
        String idempotencyKey,
        String reason,
        List<WarehouseAllocation> allocations,
        LocalDateTime releasedAt
) {
    public record WarehouseAllocation(
            String warehouseCode,
            List<Item> items
    ) {}

    public record Item(
            String productCode,
            int quantity
    ) {}

    public static InventoryReleased of(InventoryAllocationDTO allocation, String reason) {
        List<WarehouseAllocation> warehouses = allocation.allocations().stream()
                .map(wh -> new WarehouseAllocation(
                        wh.warehouseCode(),
                        wh.items().stream()
                                .map(item -> new Item(item.productCode(), item.quantity()))
                                .toList()
                ))
                .toList();

        return new InventoryReleased(
                allocation.orderNumber(),
                allocation.idempotencyKey(),
                reason,
                warehouses,
                LocalDateTime.now()
        );
    }
}
