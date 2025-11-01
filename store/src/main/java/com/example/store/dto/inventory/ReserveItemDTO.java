package com.example.store.dto.inventory;

import com.example.store.dto.order.OrderItemDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReserveItemDTO (
    @NotBlank(message = "Product code is required")
    @Size(min = 30, max = 30, message = "Product code must be exactly 30 characters long")
    String productCode,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    int quantity
) {

    public static ReserveItemDTO ofOrderItem(OrderItemDTO dto) {
        return new ReserveItemDTO(
                dto.getProductCodeAtPurchase(),
                dto.getQuantity()
        );
    }
}



