package com.example.store.enums;

public enum OrderStatus {
    PENDING,
    AWAIT_INVENTORY,
    RESERVED_AND_AWAIT_PAYMENT,
    PAID_AND_AWAIT_SHIPMENT,
    SHIPPED,
    AWAIT_REFUND_THEN_RELEASE,
    AWAIT_RELEASE_THEN_CANCEL,
    CANCELLED,
    ERROR_DEAD_LETTER     // technical issue, human/ops
}