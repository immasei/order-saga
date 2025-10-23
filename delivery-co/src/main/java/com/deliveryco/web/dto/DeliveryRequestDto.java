package com.deliveryco.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record DeliveryRequestDto(
        @NotBlank String externalOrderId,
        @NotBlank String customerId,
        String pickupWarehouseId,
        @NotBlank String pickupAddress,
        @NotBlank String dropoffAddress,
        @Email String contactEmail,
        double lossRate,
        @NotEmpty List<DeliveryRequestItemDto> items
) {

    public record DeliveryRequestItemDto(
            @NotBlank String sku,
            String description,
            @Positive int quantity
    ) {
    }
}

