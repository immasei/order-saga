package com.example.store.kafka.saga;

import com.example.store.kafka.command.ChargePayment;
import com.example.store.kafka.command.RefundPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(
    id = "payments-handler",
    topics = "#{kafkaTopicProperties.paymentsCommands()}",
    groupId = "payments-handler"
)
@RequiredArgsConstructor
public class PaymentHandler {
    // === Consume ChargePayment command
    // === Outbox PaymentSucceeded or PaymentFailed
    @KafkaHandler
    public void on(@Payload ChargePayment cmd) {
        log.warn(cmd.toString()); // tmp remove later
        // TODO
    }

    // === Consume RefundPayment command
    // === Outbox PaymentSucceeded or PaymentFailed
    @KafkaHandler
    public void on(@Payload RefundPayment cmd) {
        log.warn(cmd.toString()); // tmp remove later
        // TODO
    }
}
