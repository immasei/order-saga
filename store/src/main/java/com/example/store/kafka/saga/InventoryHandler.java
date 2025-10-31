package com.example.store.kafka.saga;

import com.example.store.config.KafkaTopicProperties;
import com.example.store.dto.inventory.InventoryAllocationDTO;
import com.example.store.enums.AggregateType;
import com.example.store.enums.ReservationStatus;
import com.example.store.exception.InsufficientStockException;
import com.example.store.exception.ResourceNotFoundException;
import com.example.store.kafka.command.ReleaseInventory;
import com.example.store.kafka.command.ReserveInventory;
import com.example.store.kafka.event.InventoryOutOfStock;
import com.example.store.kafka.event.InventoryReleased;
import com.example.store.kafka.event.InventoryReserved;
import com.example.store.model.Outbox;
import com.example.store.service.OutboxService;
import com.example.store.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final KafkaTopicProperties kafkaTopicProperties;

    // === Consume ReserveInventory command
    // === Outbox InventoryReserved or InventoryOutOfStock
    @KafkaHandler
    @Transactional(noRollbackFor = InsufficientStockException.class)
    public void on(@Payload @Valid ReserveInventory cmd) {
        log.info("@ ReserveInventory: order={} items={} createdAt={}", cmd.orderNumber(), cmd.items().size(), cmd.createdAt());

        try {
            InventoryAllocationDTO allocation = warehouseService.reserveInventory(cmd);

            InventoryReserved evt = InventoryReserved.of(allocation);
            emitEvent(cmd.orderNumber(), evt.getClass(), evt);

        } catch (InsufficientStockException ex) {  // no rollback for business failure
            log.warn(
                "@ ReserveInventory: Inventory insufficient for order={} missing={}",
                cmd.orderNumber(), ex.getMissing())
            ;
            InventoryOutOfStock evt = InventoryOutOfStock.of(
                cmd.orderNumber(), ex.getMissing()
            );
            emitEvent(cmd.orderNumber(), evt.getClass(), evt);

        } catch (Exception ex) {
            log.error("@ ReserveInventory: Failed to reserve inventory for order={}: {}", cmd.orderNumber(), ex.getMessage(), ex);
            throw ex;
        }
    }

    // === Consume ReleaseInventory command
    // === Outbox InventoryReleased
    @KafkaHandler
    public void on(@Payload @Valid ReleaseInventory cmd) {
        log.info("@ ReleaseInventory: order={} reason={}", cmd.orderNumber(), cmd.reason());

        try {
            InventoryAllocationDTO allocation = warehouseService.releaseReservation(
                    cmd.orderNumber(),
                    cmd.reason());

            InventoryReleased evt = InventoryReleased.of(allocation, cmd.reason());
            emitEvent(cmd.orderNumber(), evt.getClass(), evt);

        } catch (ResourceNotFoundException ex) {
            log.warn("@ ReleaseInventory: Reservation not found when releasing inventory for order={}, treating as idempotent", cmd.orderNumber());
            InventoryAllocationDTO empty = new InventoryAllocationDTO(
                    cmd.orderNumber(),
                    cmd.idempotencyKey(),
                    ReservationStatus.RELEASED,
                    List.of());
            InventoryReleased evt = InventoryReleased.of(empty, cmd.reason());
            emitEvent(cmd.orderNumber(), evt.getClass(), evt);

        } catch (Exception ex) {
            log.error("@ ReleaseInventory: Failed to release inventory for order={}: {}", cmd.orderNumber(), ex.getMessage(), ex);
            throw ex;
        }
    }

    // - helper
    private void emitEvent(String aggregateId, Class<?> type, Object payload) {
        Outbox outbox = new Outbox();
        outbox.setAggregateId(aggregateId);
        outbox.setAggregateType(AggregateType.INVENTORY);
        outbox.setEventType(type.getName());
        outbox.setTopic(kafkaTopicProperties.inventoryEvents());
        outboxService.save(outbox, payload);
    }
}
