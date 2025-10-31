package com.deliveryco.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

public record DeliveryRequestDto(
        @NotBlank String externalOrderId,
        @NotBlank String customerId,
        // Map of warehouseId -> pickupAddress (supports multiple warehouses)
        @NotEmpty Map<String, String> pickupLocations,
        @NotBlank String dropoffAddress,
        @Email String contactEmail,
        double lossRate,
        // Map of warehouseCode -> (productId -> quantity)
        @NotEmpty Map<String, Map<String, Integer>> items
) {

    // Backward compatibility helper (not used directly; kept to avoid breaking imports)
    public record DeliveryRequestItemDto(
            @NotBlank String sku,
            String description,
            @Positive int quantity
    ) {
    }
}
