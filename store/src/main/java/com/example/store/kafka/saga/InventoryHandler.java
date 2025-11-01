package com.example.store.kafka.saga;

import com.example.store.exception.InsufficientStockException;
import com.example.store.exception.ResourceNotFoundException;
import com.example.store.kafka.command.ReleaseInventory;
import com.example.store.kafka.command.ReserveInventory;
import com.example.store.service.ReservationService;
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
    id = "inventory-handler",
    topics = "#{kafkaTopicProperties.inventoryCommands()}",
    groupId = "inventory-handler"
)
@RequiredArgsConstructor
public class InventoryHandler {

    private final ReservationService reservationService;

    // === Consume ReserveInventory command
    // === Outbox InventoryReserved or InventoryOutOfStock
    @KafkaHandler
    public void on(@Payload @Valid ReserveInventory cmd) {
        log.info("@ ReserveInventory: [STORE][SUCCESS] for order={} items={} createdAt={}", cmd.orderNumber(), cmd.items().size(), cmd.createdAt());

        try {
            // mark reserved
            reservationService.reserveInventory(cmd);

        } catch (InsufficientStockException ex) {
            log.warn("@ ReserveInventory: [STORE][FAILED] Inventory insufficient for order={} missing={}", cmd.orderNumber(), ex.getMissing());
            // mark out of stock
            reservationService.markInventoryOutOfStock(cmd, ex.getMissing());

        } catch (Exception ex) {
            log.error("@ ReserveInventory: [STORE][UNEXPECTED] Failed to reserve inventory for order={}: {}", cmd.orderNumber(), ex.getMessage(), ex);
            throw ex;
        }
    }

    // === Consume ReleaseInventory command
    // === Outbox InventoryReleased
    @KafkaHandler
    public void on(@Payload @Valid ReleaseInventory cmd) {
        try {
            reservationService.releaseReservation(cmd); // mark released
            log.info("@ ReleaseInventory: [STORE][SUCCESS] fir order={} reason={}", cmd.orderNumber(), cmd.reason());

        } catch (ResourceNotFoundException ex) {
            log.warn("@ ReleaseInventory: [STORE][FAILED] Reservation not found when releasing inventory for order={}, treating as idempotent", cmd.orderNumber());
            reservationService.markOrphanInventoryRelease(cmd);

        } catch (Exception ex) {
            log.error("@ ReleaseInventory: [STORE][UNEXPECTED] Failed to release inventory for order={}: {}", cmd.orderNumber(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
