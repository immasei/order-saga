package com.deliveryco.messaging.dto;

import java.util.List;

public record DeliveryRequestMessage(
        String externalOrderId,
        String customerId,
        String pickupWarehouseId,
        String pickupAddress,
        String dropoffAddress,
        String contactEmail,
        double lossRate,
        List<DeliveryRequestItemMessage> items
) {

    public record DeliveryRequestItemMessage(
            String sku,
            String description,
            int quantity
    ) {
    }
}

