package com.example.store.kafka.command;

import com.example.store.kafka.event.InventoryReserved;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChargePayment (
    @NotBlank @Size(max = 30) String orderNumber,
    @NotBlank String idempotencyKey,
    LocalDateTime createdAt
) {
    public static ChargePayment of(InventoryReserved evt) {
        return ChargePayment.builder()
                .orderNumber(evt.orderNumber())
                .idempotencyKey(evt.idempotencyKey())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
