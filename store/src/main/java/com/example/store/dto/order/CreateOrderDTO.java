package com.example.store.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateOrderDTO {

    @NotNull
    private UUID customerId;

    @NotBlank(message = "Delivery address is required")
    @Size(max = 255, message = "Delivery address is too long")
    private String deliveryAddress;

    @NotNull(message = "Subtotal is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Subtotal must be positive.")
    private BigDecimal shipping;

    @NotNull(message = "Subtotal is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Subtotal must be positive.")
    private BigDecimal tax;

    @Valid
    @NotEmpty
    List<OrderItemDTO> orderItems;

}
