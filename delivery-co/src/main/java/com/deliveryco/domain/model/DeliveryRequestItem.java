package com.deliveryco.domain.model;

public record DeliveryRequestItem(
        String sku,
        String description,
        int quantity
) {
}

