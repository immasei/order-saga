package com.example.emailservice.messaging;

import com.example.emailservice.messaging.dto.DeliveryStatusMessage;
import com.example.emailservice.model.EmailMessage;
import com.example.emailservice.repository.EmailMessageRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryStatusListener {

    private final EmailMessageRepository emailMessageRepository;


    @Value("${email.default-to:demo@customer.local}")
    private String defaultToAddress;

    @KafkaListener(topics = "${email.topics.status}")
    public void onStatus(DeliveryStatusMessage msg) {
        if (msg == null) return;

        String to = normalizeToAddress(extractToAddress(msg));
        String subject = subjectFor(msg.status());
        String body = bodyFor(msg);


        if (emailMessageRepository.existsByToAddressAndExternalOrderIdAndMessageType(to, msg.externalOrderId(), msg.status())) {
            log.info("[EmailService] Skipping duplicate email for {} order {} status {}", to, msg.externalOrderId(), msg.status());
            return;
        }

        EmailMessage email = EmailMessage.builder()
                .orderId(null)
                .toAddress(to)
                .externalOrderId(msg.externalOrderId())
                .subject(subject)
                .body(body)
                .status("SENT")
                .messageType(msg.status())
                .createdAt(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .build();
        try {
            emailMessageRepository.save(email);
            log.info("[EmailService] Saved email to {} for status {} order {}", to, msg.status(), msg.externalOrderId());
        } catch (DataIntegrityViolationException e) {

            log.info("[EmailService] Duplicate detected by DB constraint for {} order {} status {} — skipped", to, msg.externalOrderId(), msg.status());


        }
    }

    private String extractToAddress(DeliveryStatusMessage msg) {
        Object fromPayload = msg.payload() != null ? msg.payload().get("contactEmail") : null;
        if (fromPayload instanceof String s && !s.isBlank()) return s;
        return defaultToAddress;
    }

    private String normalizeToAddress(String to) {
        if (to == null) return null;

        return to.trim().toLowerCase();


    }

    private String subjectFor(String status) {
        return switch (status) {
            case "PICKED_UP" -> "We’ve received your items";
            case "IN_TRANSIT" -> "Your order is on the way";
            case "DELIVERED" -> "Delivery complete";
            case "LOST" -> "Issue with your delivery";
            default -> "Delivery update";
        };
    }

    private String bodyFor(DeliveryStatusMessage msg) {
        return switch (msg.status()) {
            case "PICKED_UP" -> "Your order %s has been picked up and is now at our depot.".formatted(msg.externalOrderId());
            case "IN_TRANSIT" -> "Your order %s is on the truck and headed to you.".formatted(msg.externalOrderId());
            case "DELIVERED" -> "DeliveryCo reported your order %s has been delivered.".formatted(msg.externalOrderId());
            case "LOST" -> "There was an issue delivering %s. We’ll follow up soon.".formatted(msg.externalOrderId());
            default -> "Update for order %s: %s".formatted(msg.externalOrderId(), msg.status());
        };
    }
}

