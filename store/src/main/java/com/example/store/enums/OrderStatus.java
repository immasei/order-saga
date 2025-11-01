package com.example.store.enums;

public enum OrderStatus {
    PENDING,
    RESERVED,
    CANCELLED,
    AWAIT_INVENTORY,
    RESERVED_AND_AWAIT_PAYMENT,
    PAID_AND_AWAIT_SHIPMENT,
    SHIPPED,
    ERROR_DEAD_LETTER     // technical issue, human/ops
}