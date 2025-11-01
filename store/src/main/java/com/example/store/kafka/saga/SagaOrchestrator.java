package com.example.store.kafka.saga;

import com.example.store.config.KafkaTopicProperties;
import com.example.store.enums.AggregateType;
import com.example.store.kafka.command.*;
import com.example.store.kafka.event.*;
import com.example.store.model.Outbox;
import com.example.store.service.OrchestratorService;
import com.example.store.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.messaging.handler.annotation.Payload;

@Slf4j
@Component
@KafkaListener(
    id = "order-saga",
    topics = {
        "#{kafkaTopicProperties.ordersEvents()}",
        "#{kafkaTopicProperties.inventoryEvents()}",
        "#{kafkaTopicProperties.paymentsEvents()}",
        "#{kafkaTopicProperties.shippingEvents()}",
        "#{kafkaTopicProperties.notificationsEvents()}"
    },
    groupId = "order-saga"
)
@RequiredArgsConstructor
public class SagaOrchestrator {
    // === the only class that send Command
    private final OrchestratorService orchestrator;

    // === Kickoff ===
    //  Consume OrderCreated event
    //  Outbox ReserveInventory
    @KafkaHandler
    public void on(@Payload OrderPlaced evt) {
        log.info("@ OrderPlaced for order={}", evt.orderNumber());
        orchestrator.onOrderPlaced(evt);
    }

    // === Inventory outcomes ===
    //  Consume InventoryReserved event
    //  Outbox ChargePayment
    @KafkaHandler
    public void on(@Payload InventoryReserved evt) {
        log.info("@ InventoryReserved: [SAGA][FORWARD] for order={}", evt.orderNumber());
        orchestrator.onInventoryReserved(evt);
    }

    //  Consume InventoryOutOfStock event
    //  Outbox CancelOrder
    @KafkaHandler
    public void on(@Payload InventoryOutOfStock evt) {
        log.info("@ InventoryOutOfStock: [SAGA][COMPENSATION] for order={}", evt.orderNumber());
        orchestrator.onInventoryOutOfStock(evt);
    }

    //  Consume InventoryReleased event
    //  Outbox cancel and notify
    @KafkaHandler
    public void on(@Payload InventoryReleased evt) {
        log.info("@ InventoryReleased: [SAGA][FORWARD] for order={}", evt.orderNumber());
        orchestrator.onInventoryReleased(evt);
    }

    //  Consume InventoryReleased event
    //  Outbox cancel and notify
    @KafkaHandler
    public void on(@Payload InventoryReleaseRejected evt) {
        log.info("@ InventoryReleaseRejected: [SAGA][FORWARD] for order={}", evt.orderNumber());
        orchestrator.onInventoryReleaseRejected(evt);
    }

    // === Payment outcomes ===
    //  Consume PaymentSucceeded event
    //  Outbox CreateShipment
    @KafkaHandler
    public void on(@Payload PaymentSucceeded evt) {
        log.info("@ PaymentSucceeded: [SAGA][FORWARD] for order={}", evt.orderNumber());
        orchestrator.onPaymentSucceeded(evt);
    }

    //  Consume PaymentFailed event
    //  Outbox CancelOrder
    @KafkaHandler
    public void on(@Payload PaymentFailed evt) {
        log.info("@ PaymentFailed: [SAGA][COMPENSATION] for order={}", evt.orderNumber());
        orchestrator.onPaymentFailed(evt);
    }

    // === Shipping outcomes ===
    //  Consume ShipmentCreated event
    //  Outbox NotifyCustomer
    @KafkaHandler
    public void on(@Payload ShipmentCreated evt) {
        log.info("@ ShipmentCreated: [SAGA][FORWARD] for order={}", evt.orderNumber());
        orchestrator.onShipmentCreated(evt);
    }

    //  Consume ShipmentFailed event
    //  Outbox CancelOrder
    @KafkaHandler
    public void on(@Payload ShipmentFailed evt) {
        log.warn(evt.toString()); // tmp fix later

    }

    // === Notification outcomes ===
    //  Consume EmailFailed event
    //  Outbox NotifyCustomer
    @KafkaHandler
    public void on(@Payload EmailFailed evt) {
        // No-op or metrics. We've already saved FAILED in markEmailFailed().
        log.debug("@ EmailFailed: [SAGA] received for order={} \nto={} \nsubject={} \nbody=\n{}", evt.orderNumber(), evt.toAddress(), evt.subject(), evt.body());
    }

    //  Consume EmailFailed event
    //  Outbox nothing
    @KafkaHandler
    public void on(@Payload EmailSent evt) {
        log.debug("@ EmailSent: [SAGA] received for order={} \nto={} \nsubject={} \nbody=\n{}", evt.orderNumber(), evt.toAddress(), evt.subject(), evt.body());
    }

}
