package com.example.store.kafka.event;

import com.example.store.dto.inventory.InventoryAllocationDTO;

import java.time.LocalDateTime;
import java.util.List;

public record InventoryReserved(
        String orderNumber,
        String idempotencyKey,
        List<WarehouseAllocation> allocations,
        LocalDateTime reservedAt
) {
    public record WarehouseAllocation(
            String warehouseCode,
            List<Item> items
    ) {}

    public record Item(
            String productCode,
            int quantity
    ) {}

    public static InventoryReserved of(InventoryAllocationDTO allocation) {
        List<WarehouseAllocation> warehouses = allocation.allocations().stream()
                .map(wh -> new WarehouseAllocation(
                        wh.warehouseCode(),
                        wh.items().stream()
                                .map(item -> new Item(item.productCode(), item.quantity()))
                                .toList()
                ))
                .toList();

        return new InventoryReserved(
                allocation.orderNumber(),
                allocation.idempotencyKey(),
                warehouses,
                LocalDateTime.now()
        );
    }
}
