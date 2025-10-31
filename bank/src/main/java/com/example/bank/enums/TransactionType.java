package com.example.bank.enums;

public enum TransactionType {
    DEPOSIT,
    TRANSFER,
    WITHDRAWAL,
    REFUND;

    @Override
    public String toString() {
        String lower = name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1) + ".";
    }
}
