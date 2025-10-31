package com.example.store.dto.inventory;

import com.example.store.enums.ReservationStatus;

import java.util.List;

public record InventoryAllocationDTO(
    String orderNumber,
    String idempotencyKey,
    ReservationStatus status,
    List<WarehouseAllocationDTO> allocations
) {
    public record WarehouseAllocationDTO(
        String warehouseCode,
        List<ItemAllocationDTO> items
    ) {}

    public record ItemAllocationDTO(
        String productCode,
        int quantity
    ) {}
}


