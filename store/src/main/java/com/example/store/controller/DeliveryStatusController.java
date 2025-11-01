package com.example.store.controller;

import com.example.store.enums.OrderStatus;
import com.example.store.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@Slf4j
public class DeliveryStatusController {

    private final OrderRepository orderRepository;

    @Value("${app.delivery.secret:}")
    private String sharedSecret;

    @PostMapping("/status-callback")
    @Transactional
    public ResponseEntity<Void> onStatus(
            @RequestHeader(name = "X-DeliveryCo-Secret", required = false) String secret,
            @RequestBody DeliveryStatusCallbackDto body
    ) {
        if (sharedSecret != null && !sharedSecret.isBlank()) {
            if (secret == null || !sharedSecret.equals(secret)) {
                return ResponseEntity.status(401).build();
            }
        }
        try {
            var order = orderRepository.findByOrderNumberOrThrow(body.externalOrderId());
            OrderStatus mapped = map(body.status());
            if (mapped != null) {
                order.setStatus(mapped);
            }
            return ResponseEntity.accepted().build();
        } catch (Exception ex) {
            log.warn("Failed to handle delivery status callback {}: {}", body, ex.toString());
            return ResponseEntity.badRequest().build();
        }
    }

    private OrderStatus map(String status) {
        if (status == null) return null;
        return switch (status) {
            case "RECEIVED" -> OrderStatus.RESERVED;
            case "PICKED_UP", "IN_TRANSIT" -> OrderStatus.SHIPPED; // or IN_DELIVERY if added
            case "DELIVERED" -> OrderStatus.SHIPPED; // or COMPLETED if added
            case "LOST", "CANCELLED" -> OrderStatus.CANCELLED;
            default -> null;
        };
    }

    public record DeliveryStatusCallbackDto(
            UUID eventId,
            String externalOrderId,
            String status,
            String reason,
            OffsetDateTime occurredAt
    ) {}
}

