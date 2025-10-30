package com.deliveryco.messaging.dto;

import java.util.Map;

public record DeliveryRequestMessage(
        String externalOrderId,
        String customerId,
        // Map of warehouseId -> pickupAddress
        Map<String, String> pickupLocations,
        String dropoffAddress,
        String contactEmail,
        double lossRate,
        // Map of warehouseCode -> (productId -> quantity)
        Map<String, Map<String, Integer>> items
) {
}
