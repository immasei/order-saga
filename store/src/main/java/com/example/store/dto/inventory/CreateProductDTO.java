package com.example.store.dto.inventory;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class CreateProductDTO {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name is too long")
    private String productName;

    @NotBlank(message = "Product description is required")
    @Size(max = 255, message = "Product description is too long")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be positive.")
    @Digits(integer = 13, fraction = 2, message = "Price must have at most 13 digits and 2 decimal places")
    private BigDecimal price;

}
