package com.example.store.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderItemDTO {

    @NotBlank(message = "Product code cannot be blank")
    @Size(max = 30, message = "Product code doesn't exist")
    private String productCode;

    @NotNull(message = "Quantity is required.")
    @Min(value = 0, message = "Quantity cannot be negative")
    private int quantity;

}
