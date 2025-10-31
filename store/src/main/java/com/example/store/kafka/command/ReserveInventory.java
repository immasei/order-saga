package com.example.store.kafka.command;

import com.example.store.dto.inventory.ReserveItemDTO;
import com.example.store.kafka.event.OrderPlaced;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ReserveInventory (
    @NotBlank @Size(max = 30) String orderNumber,
    @Valid @NotEmpty List<ReserveItemDTO> items,
    @NotBlank String idempotencyKey,
    LocalDateTime createdAt
) {
    public static ReserveInventory of(OrderPlaced evt) {
        return ReserveInventory.builder()
            .orderNumber(evt.orderNumber())
            .items(evt.items())
            .idempotencyKey(evt.idempotencyKey())
            .createdAt(LocalDateTime.now())
            .build();
    }
}
