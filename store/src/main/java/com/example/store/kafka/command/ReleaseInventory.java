package com.example.store.kafka.command;

import com.example.store.enums.EventType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReleaseInventory(
        String orderNumber,
        String idempotencyKey,
        EventType reason,
        LocalDateTime createdAt
) {}
