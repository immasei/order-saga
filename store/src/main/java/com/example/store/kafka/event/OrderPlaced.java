package com.example.store.kafka.event;

import com.example.store.dto.inventory.ReserveItemDTO;
import com.example.store.dto.order.OrderDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderPlaced(
    @NotBlank @Size(max = 30) String orderNumber,
    @NotBlank @Email String customerEmail,
    @Valid @NotEmpty List<ReserveItemDTO> items,
    @NotBlank String idempotencyKey,
    LocalDateTime createdAt
) {
    public static OrderPlaced of(OrderDTO order, String idempotencyKey) {
        List<ReserveItemDTO> items = order.getOrderItems().stream()
            .map(ReserveItemDTO::ofOrderItem)
            .toList();

        return OrderPlaced.builder()
            .orderNumber(order.getOrderNumber())
            .customerEmail(order.getCustomerEmail())
            .items(items)
            .idempotencyKey(idempotencyKey)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
