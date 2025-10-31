package com.example.store.kafka.saga;

import com.example.store.kafka.command.CreateShipment;
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
    id = "shipping-handler",
    topics = "#{kafkaTopicProperties.shippingCommands()}",
    groupId = "shipping-handler"
)
@RequiredArgsConstructor
public class ShippingHandler {

    // === Consume CreateShipment command
    // === Outbox ShipmentCreated or ShipmentFailed
    @KafkaHandler
    public void on(@Payload @Valid CreateShipment cmd) {
        log.warn(cmd.toString()); // tmp remove later
        // TODO
    }

}
