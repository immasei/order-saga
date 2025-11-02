package com.example.store.enums;

public enum PaymentStatus {
    PENDING,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    REFUND_REJECTED, // also means payment succeed be4
    REFUND_SUCCESS // also means payment succeed be4
}