package com.example.store.kafka.command;

import com.example.store.enums.EventType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotifyCustomer (
        String orderNumber,
        String toAddress,
        String subject,
        String body,
        EventType reason,
        LocalDateTime createdAt
) {}
