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

    // if not provided, will use customer address (saved during create account)
    // @NotBlank(message = "Delivery address is required")
    @Size(max = 255, message = "Delivery address is too long")
    private String deliveryAddress;

    @NotNull(message = "Shipping fee is required.")
    @DecimalMin(value = "0.0", message = "Shipping fee must be positive.")
    @Digits(integer = 13, fraction = 2, message = "Shipping fee must have at most 13 digits and 2 decimal places")
    private BigDecimal shipping;

    @Valid
    @NotEmpty
    List<CreateOrderItemDTO> orderItems;

    // customer's bank account ref for payment
    @NotBlank(message = "Payment account reference is required")
    @Size(max = 100, message = "Payment account reference is too long")
    private String paymentAccountRef;

}
