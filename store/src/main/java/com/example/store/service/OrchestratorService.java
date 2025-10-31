package com.example.store.service;

import com.example.store.config.KafkaTopicProperties;
import com.example.store.enums.AggregateType;
import com.example.store.enums.OrderStatus;
import com.example.store.kafka.command.ChargePayment;
import com.example.store.kafka.command.CreateShipment;
import com.example.store.kafka.command.ReserveInventory;
import com.example.store.kafka.event.InventoryReserved;
import com.example.store.kafka.event.OrderPlaced;
import com.example.store.kafka.event.PaymentSucceeded;
import com.example.store.model.Order;
import com.example.store.model.Outbox;
import com.example.store.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private final OutboxService outboxService;
    private final KafkaTopicProperties kafkaProps;
    private final OrderRepository orderRepository;

    @Transactional
    public void onOrderPlaced(OrderPlaced evt) {
        Order order = orderRepository
                .findByOrderNumberForUpdateOrThrow(evt.orderNumber());
        order.setStatus(OrderStatus.AWAIT_INVENTORY);
        orderRepository.save(order);

        // 1. build command
        ReserveInventory cmd = ReserveInventory.of(evt);

        // 2. outbox ReserveInventory to db
        //    this event will later be published by kafka/OutboxPublisher
        Outbox outbox = new Outbox();
        outbox.setAggregateId(cmd.orderNumber());
        outbox.setAggregateType(AggregateType.INVENTORY);
        outbox.setEventType(cmd.getClass().getName());
        outbox.setTopic(kafkaProps.inventoryCommands());
        outboxService.save(outbox, cmd);
    }

    @Transactional
    public void onInventoryReserved(InventoryReserved evt) {
        Order order = orderRepository
                .findByOrderNumberForUpdateOrThrow(evt.orderNumber());
        order.setStatus(OrderStatus.RESERVED_AND_AWAIT_PAYMENT);
        orderRepository.save(order);

        // 1. build command
        ChargePayment cmd = ChargePayment.of(evt);

        // 2. outbox ChargePayment to db
        Outbox outbox = new Outbox();
        outbox.setAggregateId(cmd.orderNumber());
        outbox.setAggregateType(AggregateType.PAYMENT);
        outbox.setEventType(cmd.getClass().getName());
        outbox.setTopic(kafkaProps.paymentsCommands());
        outboxService.save(outbox, cmd);
    }

    @Transactional void onPaymentSucceeded(PaymentSucceeded evt) {
//        Order order = orderRepository
//                .findByOrderNumberForUpdateOrThrow(evt.orderNumber());
//        order.setStatus(OrderStatus.PAID_AND_AWAIT_SHIPMENT);
//        orderRepository.save(order);
//
//
//
//        // 1. build command
//        CreateShipment cmd = CreateShipment.of(evt);
//
//        // 2. outbox ChargePayment to db
//        Outbox outbox = new Outbox();
//        outbox.setAggregateId(cmd.orderNumber());
//        outbox.setAggregateType(AggregateType.SHIPMENT);
//        outbox.setEventType(cmd.getClass().getName());
//        outbox.setTopic(kafkaProps.shippingCommands());
//        outboxService.save(outbox, cmd);

    }
}

