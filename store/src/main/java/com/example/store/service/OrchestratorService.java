package com.example.store.service;

import com.example.store.config.KafkaTopicProperties;
import com.example.store.dto.inventory.InventoryAllocationDTO;
import com.example.store.enums.AggregateType;
import com.example.store.enums.EventType;
import com.example.store.enums.OrderStatus;
import com.example.store.enums.RefundOutcome;
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
    private final OrderService orderService;

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
        Order order = orderService
                .updateOrderStatus(evt.orderNumber(), OrderStatus.RESERVED_AND_AWAIT_PAYMENT);

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
        Order order = orderService.updateOrderStatus(
                evt.orderNumber(), OrderStatus.PAID_AND_AWAIT_SHIPMENT
        );

        InventoryAllocationDTO reservation =
                reservationService.getReservation(evt.orderNumber());

        Map<String, String> pickupLocations =
                reservationService.getPickupLocations(reservation);

        Map<String, Map<String, Integer>> productsByWarehouse =
                reservationService.getPickedItemsByWarehouse(reservation);

        // 1. build command
        CreateShipment cmd = CreateShipment.of(order, evt, pickupLocations, productsByWarehouse);

        // 2. outbox CreateShipment to db
        Outbox outbox = new Outbox();
        outbox.setAggregateId(cmd.orderNumber());
        outbox.setAggregateType(AggregateType.SHIPMENT);
        outbox.setEventType(cmd.getClass().getName());
        outbox.setTopic(kafkaProps.shippingCommands());
        outboxService.save(outbox, cmd);
    }

    @Transactional
    public void onShipmentCreated(ShipmentCreated evt) {
        // status update to SHIPPED
        Order order = orderService
                .updateDeliveryTrackingId(evt.orderNumber(), evt.deliveryTrackingId());

        // reservation COMMITTED
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
        Order order = orderService
                .updateOrderStatus(evt.orderNumber(), OrderStatus.CANCELLED);

        String body = """
            Order Update: %s

            We regret to inform you that one or more items in your order are no longer in stock.

            Inventory Status: OUT OF STOCK
            Order Status: Cancelled

            Thank you for your understanding.
            """.formatted(
                evt.orderNumber(),
                EventType.INVENTORY_OUT_OF_STOCK
        );

        // 1. notify customer
        NotifyCustomer cmd = NotifyCustomer.builder()
            .orderNumber(evt.orderNumber())
            .toAddress(order.getCustomer().getEmail())
            .subject(String.format("[CANCELLED] ORDER %s", evt.orderNumber()))
            .body(body)
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
    public void onPaymentFailed(PaymentFailed evt) {
        orderService.updateOrderStatus(
                evt.orderNumber(), OrderStatus.AWAIT_RELEASE_THEN_CANCEL);

        // 1. creat cmd
        ReleaseInventory cmd = ReleaseInventory.of(evt);

        // 2. outbox ReleaseInventory to db
        Outbox outbox = new Outbox();
        outbox.setAggregateId(cmd.orderNumber());
        outbox.setAggregateType(AggregateType.INVENTORY);
        outbox.setEventType(cmd.getClass().getName());
        outbox.setTopic(kafkaProps.inventoryCommands());
        outboxService.save(outbox, cmd);
    }

    @Transactional
    public void onInventoryReleased(InventoryReleased evt) {
        RefundOutcome refundOutcome = evt.refundOutcome();
        EventType refundOutcomeCause = evt.refundOutcomeCause();

        OrderStatus status = (refundOutcome == null)
                ? OrderStatus.CANCELLED
                : switch (refundOutcome) {
            case SUCCESS, NO_ACTION_REQUIRED -> OrderStatus.CANCELLED_REFUNDED;
            case PROVIDER_ERROR -> OrderStatus.CANCELLED_REQUIRES_MANUAL_REFUND;
            default -> OrderStatus.CANCELLED;
        };

        Order order = orderService.updateOrderStatus(evt.orderNumber(), status);

        String refundSection = (refundOutcome != null)
                ? """
              
            Refund Outcome: %s (%s)
            """.formatted(refundOutcome, refundOutcomeCause)
                : "";

        String body = """
            Order Update: %s

            Triggered By: %s
            Inventory Release Outcome: %s (%s)%s
            Current Order Status: %s

            Thank you for shopping with us.
            """.formatted(
                evt.orderNumber(),
                evt.triggerBy(),
                evt.releaseOutcome(),
                evt.releaseOutcomeCause(),
                refundSection,
                status
        );

        // 1. notify customer
        NotifyCustomer cmd = NotifyCustomer.builder()
                .orderNumber(evt.orderNumber())
                .toAddress(order.getCustomer().getEmail())
                .subject(String.format("[CANCELLED] ORDER %s", evt.orderNumber()))
                .body(body)
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
    public void onInventoryReleaseRejected(InventoryReleaseRejected evt) {
        Order order = orderRepository
                .findByOrderNumberForUpdateOrThrow(evt.orderNumber());

        String body = """
            Order Update: %s
    
            Triggered By: %s
            Inventory Release Outcome: %s(%s)
            Current Order Status: %s
    
            Thank you for shopping with us.
        """.formatted(
                evt.orderNumber(),
                evt.triggerBy(),
                evt.releaseOutcome(),
                evt.releaseOutcomeCause(),
                order.getStatus()
        );

        // 1. notify customer
        NotifyCustomer cmd = NotifyCustomer.builder()
                .orderNumber(evt.orderNumber())
                .toAddress(order.getCustomer().getEmail())
                .subject(String.format("[UNABLE TO CANCEL] ORDER %s", evt.orderNumber()))
                .body(body)
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
    public void onShipmentFailed(ShipmentFailed evt) {
        Order order = orderService.updateOrderStatus(
                evt.orderNumber(), OrderStatus.AWAIT_REFUND_THEN_RELEASE);

        RefundPayment cmd = RefundPayment.of(order, evt);

        // 2. outbox ReleaseInventory to db
        Outbox outbox = new Outbox();
        outbox.setAggregateId(cmd.orderNumber());
        outbox.setAggregateType(AggregateType.PAYMENT);
        outbox.setEventType(cmd.getClass().getName());
        outbox.setTopic(kafkaProps.paymentsCommands());
        outboxService.save(outbox, cmd);
    }

    @Transactional
    public void onPaymentRefunded(PaymentRefunded evt) {
        orderService.updateOrderStatus(
                evt.orderNumber(), OrderStatus.AWAIT_RELEASE_THEN_CANCEL);

        // 1. creat cmd
        ReleaseInventory cmd = ReleaseInventory.of(evt);

        // 2. outbox ReleaseInventory to db
        Outbox outbox = new Outbox();
        outbox.setAggregateId(cmd.orderNumber());
        outbox.setAggregateType(AggregateType.INVENTORY);
        outbox.setEventType(cmd.getClass().getName());
        outbox.setTopic(kafkaProps.inventoryCommands());
        outboxService.save(outbox, cmd);
    }

    @Transactional
    public void onPaymentRefundRejected(PaymentRefundRejected evt) {
        Order order = orderRepository
                .findByOrderNumberForUpdateOrThrow(evt.orderNumber());

        String body = """
            Order Update: %s
    
            Triggered By: %s
            Inventory Release Outcome: %s(%s)
            Current Order Status: %s
    
            Thank you for shopping with us.
        """.formatted(
                evt.orderNumber(),
                evt.triggerBy(),
                evt.outcome(),
                evt.reason(),
                order.getStatus()
        );

        // 1. notify customer
        NotifyCustomer cmd = NotifyCustomer.builder()
                .orderNumber(evt.orderNumber())
                .toAddress(order.getCustomer().getEmail())
                .subject(String.format("[UNABLE TO CANCEL] ORDER %s", evt.orderNumber()))
                .body(body)
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

