package com.example.store.kafka.saga;

import com.example.store.exception.ConflictException;
import com.example.store.exception.InsufficientStockException;
import com.example.store.exception.ReleaseNotAllowedException;
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
        try {
            // mark reserved
            reservationService.reserveInventory(cmd);
            log.info("@ ReserveInventory: [STORE][SUCCESS] for order={} items={} createdAt={}", cmd.orderNumber(), cmd.items().size(), cmd.createdAt());

        } catch (InsufficientStockException ex) {
            log.warn("@ ReserveInventory: [STORE][FAILED] Inventory insufficient for order={} missing={}", cmd.orderNumber(), ex.getMissing());
            // mark out of stock
            reservationService.markInventoryOutOfStock(cmd, ex.getMissing());

        } catch (ConflictException ex) {
            // An order with this orderNumber already exists, but its idempotency key doesn’t match
            // this is not the same request being retried, it’s a logically different command for the same order

            log.warn("@ ReserveInventory: [STORE][IGNORED] Idempotency conflict for order={} message={}", cmd.orderNumber(), ex.getMessage());
            // No event emitted; commit & ack message

        } catch (Exception ex) {
            log.error("@ ReserveInventory: [STORE][UNEXPECTED] Failed to reserve inventory for order={}: {}", cmd.orderNumber(), ex.getMessage());
        }
    }

    // === Consume ReleaseInventory command
    // === Outbox InventoryReleased
    @KafkaHandler
    public void on(@Payload @Valid ReleaseInventory cmd) {
        try {
            reservationService.releaseReservation(cmd); // mark released
            log.info("@ ReleaseInventory: [STORE][SUCCESS] for order={} reason={}", cmd.orderNumber(), cmd.reason());

        } catch (ResourceNotFoundException ex) {
            log.warn("@ ReleaseInventory: [STORE][NOOP] Reservation not found when releasing inventory for order={}, treating as idempotent", cmd.orderNumber());
            reservationService.markOrphanInventoryRelease(cmd);

        } catch (ReleaseNotAllowedException ex) {
            log.warn("@ ReleaseInventory: [STORE][REJECTED] Reservation already committed={}, unable to release: {}", cmd.orderNumber(), ex.getMessage());
            reservationService.markInventoryReleaseRejected(cmd);

        } catch (Exception ex) {
            log.error("@ ReleaseInventory: [STORE][UNEXPECTED] Failed to release inventory for order={}: {}", cmd.orderNumber(), ex.getMessage());
        }
    }
}
