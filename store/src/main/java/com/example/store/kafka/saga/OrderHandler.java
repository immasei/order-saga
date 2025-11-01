package com.example.store.kafka.saga;

import com.example.store.config.KafkaTopicProperties;
import com.example.store.dto.order.CreateOrderDTO;
import com.example.store.dto.order.OrderDTO;
import com.example.store.enums.AggregateType;
import com.example.store.kafka.command.CancelOrder;
import com.example.store.kafka.event.OrderPlaced;
import com.example.store.model.Outbox;
import com.example.store.service.OrderService;
import com.example.store.service.OutboxService;
import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.transaction.Transactional;
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
    id = "orders-handler",
    topics = "#{kafkaTopicProperties.ordersCommands()}",
    groupId = "orders-handler"
)
@RequiredArgsConstructor
public class OrderHandler {

    private final OrderService orderService;

    // === Consume CancelOrder command
    // (Based on Order.status)
    // 1. Cancel all pending/failed outbox events
    // 2. Outbox
    //      - ReleaseInventory event
    //      - RefundPayment event
    //      - NotifyCustomer event
    @KafkaHandler
    public void on(@Payload @Valid CancelOrder cmd) {
        log.warn(cmd.toString()); // tmp remove later
        // TODO
    }

}
