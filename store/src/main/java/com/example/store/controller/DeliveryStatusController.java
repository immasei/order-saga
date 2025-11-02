package com.example.store.controller;

import com.example.store.dto.delivery.DeliveryStatusCallbackDTO;
import com.example.store.service.ShippingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@Slf4j
public class DeliveryStatusController {

    private final ShippingService shippingService;

    @Value("${app.delivery.secret:}")
    private String sharedSecret;

    @PostMapping("/status-callback")
    @Transactional
    public ResponseEntity<Void> onStatus(
            @RequestHeader(name = "X-DeliveryCo-Secret", required = false) String secret,
            @RequestBody DeliveryStatusCallbackDTO body
    ) {
        if (sharedSecret != null && !sharedSecret.isBlank()) {
            if (secret == null || !sharedSecret.equals(secret)) {
                return ResponseEntity.status(401).build();
            }
        }
        try {
            shippingService.updateDeliveryStatus(body);
            return ResponseEntity.accepted().build();
        } catch (Exception ex) {
            log.warn("Failed to handle delivery status callback {}: {}", body, ex.toString());
            return ResponseEntity.badRequest().build();
        }
    }

}

