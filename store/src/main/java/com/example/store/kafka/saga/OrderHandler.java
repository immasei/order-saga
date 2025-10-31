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
    private final OutboxService outboxService;
    private final KafkaTopicProperties kafkaProps;

    // === Entry point: user placed order, outbox OrderPlaced ===
    @Transactional
    public OrderDTO placeOrder(CreateOrderDTO orderDto) {
        String idempotencyKey = UlidCreator.getMonotonicUlid().toString();

        // 1. save order to db
        OrderDTO order = orderService.createOrder(orderDto, idempotencyKey);

        // 2. create OrderPlaced event (aka fact)
        OrderPlaced evt = OrderPlaced.of(order, idempotencyKey);

        // 3. save OrderCreated to db, this event will later be
        //    published by kafka/OutboxPublisher
        Outbox outbox = new Outbox();
        outbox.setAggregateId(order.getOrderNumber());
        outbox.setAggregateType(AggregateType.ORDER);
        outbox.setEventType(evt.getClass().getName());
        outbox.setTopic(kafkaProps.ordersEvents());
        outboxService.save(outbox, evt);

        return order;
    }

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
