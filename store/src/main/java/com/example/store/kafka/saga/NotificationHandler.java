package com.example.store.kafka.saga;

import com.example.store.kafka.command.NotifyCustomer;
import com.example.store.service.EmailService;
import com.example.store.service.OutboxService;
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

    private final OutboxService outboxService;
    private final EmailService emailService;

    // === Consume NotifyCustomer command
    // === Outbox EmailSent or EmailFailed
    @KafkaHandler
    public void on(@Payload @Valid NotifyCustomer cmd) {
        log.warn(cmd.toString()); // tmp remove later
        // TODO
    }
}
