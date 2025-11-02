package com.example.store.kafka.saga;

import com.example.store.dto.notification.EmailResponseDTO;
import com.example.store.exception.EmailException;
import com.example.store.kafka.command.NotifyCustomer;
import com.example.store.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(
    id = "notifications-handler",
    topics = "#{kafkaTopicProperties.notificationsCommands()}",
    groupId = "notifications-handler"
)
@RequiredArgsConstructor
public class NotificationHandler {

    private final NotificationService emailService;

    // === Consume NotifyCustomer command
    // === Outbox EmailSent or EmailFailed
    @KafkaHandler
    public void on(@Payload @Valid NotifyCustomer cmd) {
        try {
            EmailResponseDTO email = emailService.email(cmd);
            emailService.markEmailSent(cmd, email);
            log.info("@ NotifyCustomer: [EMAIL][SUCCESS] for order={}, createdAt={}", cmd.orderNumber(), cmd.createdAt());

        } catch (EmailException ex) {
            log.warn("@ NotifyCustomer: [EMAIL][FAILED] for order={}, status={}, message={}, createdAt={}", cmd.orderNumber(), ex.getStatusCode(), ex.getMessage(), cmd.createdAt());
            emailService.markEmailFailed(cmd);

        } catch (Exception ex) {
            log.error("@ NotifyCustomer: [EMAIL][UNEXPECTED] Failed to notify customer for order={}: {}, createdAt={}", cmd.orderNumber(), ex.getMessage(), cmd.createdAt());
            emailService.markEmailFailed(cmd);
        }
    }
}
