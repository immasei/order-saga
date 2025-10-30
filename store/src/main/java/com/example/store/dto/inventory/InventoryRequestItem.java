package com.example.store.dto.inventory;

public record InventoryRequestItem(
        String productCode,
        int quantity
) {}


