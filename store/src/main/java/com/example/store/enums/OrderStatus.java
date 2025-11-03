package com.example.store.enums;

public enum OrderStatus {
    PENDING,
    AWAIT_INVENTORY,
    RESERVED_AND_AWAIT_PAYMENT,
    PAID_AND_AWAIT_SHIPMENT,

    DELIVERY_REQUESTED, // sent delivery request to delivery co
    AWAIT_CARRIER_PICKUP, // delivery request has been received
    IN_TRANSIT, // on a delivery truck
    OUT_FOR_DELIVERY, // on the way to customers
    DELIVERED, // order completed
    LOST_IN_DELIVERY, // etc package lost

    AWAIT_REFUND_THEN_RELEASE,
    AWAIT_RELEASE_THEN_CANCEL,
    CANCELLED,
    CANCELLED_REFUNDED,
    CANCELLED_REQUIRES_MANUAL_REFUND,

}