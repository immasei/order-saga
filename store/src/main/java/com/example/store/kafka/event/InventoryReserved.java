package com.example.store.kafka.event;

import com.example.store.dto.inventory.InventoryAllocationDTO;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record InventoryReserved(
        String orderNumber,
        String idempotencyKey,
        List<InventoryAllocationDTO.WarehouseAllocationDTO> allocations,
        LocalDateTime reservedAt
) {

    public static InventoryReserved of(InventoryAllocationDTO allocation) {
        return InventoryReserved.builder()
                    .orderNumber(allocation.orderNumber())
                    .idempotencyKey(allocation.idempotencyKey())
                    .allocations(allocation.allocations())
                    .reservedAt(LocalDateTime.now())
                    .build();
    }
}
