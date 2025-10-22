package com.deliveryco.domain.model;

import java.util.List;

public record DeliveryRequest(
        String externalOrderId,
        String customerId,
        String pickupWarehouseId,
        String pickupAddress,
        String dropoffAddress,
        String contactEmail,
        double lossRate,
        List<DeliveryRequestItem> items
) {

    public double determineLossRate(double defaultLossRate) {
        return lossRate > 0 ? lossRate : defaultLossRate;
    }
}

