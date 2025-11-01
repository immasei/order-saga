package com.example.store.dto.inventory;

import jakarta.validation.constraints.NotBlank;

public record ReleaseReservationRequest(
        @NotBlank(message = "Reason is required") String reason,
        @NotBlank(message = "Idempotency key is required") String idempotencyKey
) {}


