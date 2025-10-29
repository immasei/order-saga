package com.example.store.kafka.saga;

import com.example.store.kafka.command.ReleaseInventory;
import com.example.store.kafka.command.ReserveInventory;
import com.example.store.service.OutboxService;
import com.example.store.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(
    id = "inventory-handler",
    topics = "#{kafkaTopicProperties.inventoryCommands()}",
    groupId = "inventory-handler"
)
@RequiredArgsConstructor
public class InventoryHandler {

    private final OutboxService outboxService;
    private final WarehouseService warehouseService;

    // === Consume ReserveInventory command
    // === Outbox InventoryReserved or InventoryOutOfStock
    @KafkaHandler
    public void on(@Payload ReserveInventory cmd) {
        log.warn(cmd.toString()); // tmp remove later
        // TODO
    }

    // === Consume ReleaseInventory command
    // === Outbox InventoryReleased
    @KafkaHandler
    public void on(@Payload ReleaseInventory cmd) {
        log.warn(cmd.toString()); // tmp remove later
        // TODO
    }

}
