package com.example.store.enums;

public enum OrderStatus {
    PENDING,
    AWAIT_INVENTORY,      // sent ReserveInventory
    INVENTORY_RESERVED,   // got InventoryReserved
    RESERVED,
    PAID,
    AWAIT_SHIPMENT,       // sent CreateShipment
    SHIPPED,
    CANCELLED,
    ERROR_DEAD_LETTER     // technical issue, human/ops
}