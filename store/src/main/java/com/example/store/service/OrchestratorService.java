package com.example.store.service;

import com.example.store.config.KafkaTopicProperties;
import com.example.store.dto.inventory.InventoryAllocationDTO;
import com.example.store.enums.AggregateType;
import com.example.store.enums.EventType;
import com.example.store.enums.OrderStatus;
import com.example.store.kafka.command.*;
import com.example.store.kafka.event.*;
import com.example.store.model.Order;
import com.example.store.model.Outbox;
import com.example.store.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private final OutboxService outboxService;
    private final KafkaTopicProperties kafkaProps;
    private final OrderRepository orderRepository;
    private final ReservationService reservationService;

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
        ChargePayment cmd = ChargePayment.of(order, evt);

        // 2. outbox ChargePayment to db
        Outbox outbox = new Outbox();
        outbox.setAggregateId(cmd.orderNumber());
        outbox.setAggregateType(AggregateType.PAYMENT);
        outbox.setEventType(cmd.getClass().getName());
        outbox.setTopic(kafkaProps.paymentsCommands());
        outboxService.save(outbox, cmd);
    }

    @Transactional
    public void onPaymentSucceeded(PaymentSucceeded evt) {
        Order order = orderRepository
                .findByOrderNumberForUpdateOrThrow(evt.orderNumber());
        order.setStatus(OrderStatus.PAID_AND_AWAIT_SHIPMENT);
        orderRepository.save(order);

        InventoryAllocationDTO reservation = reservationService
                .getReservation(evt.orderNumber());

        Map<String, String> pickupLocations = reservationService
                .getPickupLocations(reservation);

        Map<String, Map<String, Integer>> productsByWarehouse = reservationService
                .getPickedItemsByWarehouse(reservation);

        // 1. build command
        CreateShipment cmd = CreateShipment.of(order, evt, pickupLocations, productsByWarehouse);

        // 2. outbox ChargePayment to db
        Outbox outbox = new Outbox();
        outbox.setAggregateId(cmd.orderNumber());
        outbox.setAggregateType(AggregateType.SHIPMENT);
        outbox.setEventType(cmd.getClass().getName());
        outbox.setTopic(kafkaProps.shippingCommands());
        outboxService.save(outbox, cmd);
    }

    @Transactional
    public void onShipmentCreated(ShipmentCreated evt) {
        Order order = orderRepository
                .findByOrderNumberForUpdateOrThrow(evt.orderNumber());
        order.setStatus(OrderStatus.SHIPPED);
        order.setDeliveryTrackingId(evt.deliveryTrackingId());
        orderRepository.save(order);

        reservationService.commitReservation(evt.orderNumber());

        // 1. notify customer
        NotifyCustomer cmd = NotifyCustomer.builder()
                .orderNumber(evt.orderNumber())
                .toAddress(order.getCustomer().getEmail())
                .subject(String.format("[SHIPPED] ORDER %s", evt.orderNumber()))
                .body(EventType.ORDER_SHIPPED.toString())
                .createdAt(LocalDateTime.now())
                .build();

        // 2. outbox NotifyCustomer to db
        Outbox outbox = new Outbox();
        outbox.setAggregateId(cmd.orderNumber());
        outbox.setAggregateType(AggregateType.NOTIFICATION);
        outbox.setEventType(cmd.getClass().getName());
        outbox.setTopic(kafkaProps.notificationsCommands());
        outboxService.save(outbox, cmd);
    }

    @Transactional
    public void onInventoryOutOfStock(InventoryOutOfStock evt) {
        Order order = orderRepository
                .findByOrderNumberForUpdateOrThrow(evt.orderNumber());
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // 1. notify customer
        NotifyCustomer cmd = NotifyCustomer.builder()
            .orderNumber(evt.orderNumber())
            .toAddress(order.getCustomer().getEmail())
            .subject(String.format("[CANCELLED] ORDER %s", evt.orderNumber()))
            .body(EventType.INVENTORY_OUT_OF_STOCK.toString())
            .createdAt(LocalDateTime.now())
            .build();

        // 2. outbox NotifyCustomer to db
        Outbox outbox = new Outbox();
        outbox.setAggregateId(cmd.orderNumber());
        outbox.setAggregateType(AggregateType.NOTIFICATION);
        outbox.setEventType(cmd.getClass().getName());
        outbox.setTopic(kafkaProps.notificationsCommands());
        outboxService.save(outbox, cmd);
    }

}

