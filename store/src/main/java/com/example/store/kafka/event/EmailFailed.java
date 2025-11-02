package com.example.store.kafka.event;

import com.example.store.enums.EventType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EmailFailed (
        String orderNumber,
        String toAddress,
        String subject,
        String body,
        EventType reason,
        LocalDateTime createdAt
) {}