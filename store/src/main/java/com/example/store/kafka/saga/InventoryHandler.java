package com.example.store.kafka.saga;

import com.example.store.exception.*;
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
            reservationService.beforeReservation(cmd); // check order status

            // mark reserved
            reservationService.reserveInventory(cmd);
            log.info("@ ReserveInventory: [STORE][SUCCESS] for order={} items={} createdAt={}", cmd.orderNumber(), cmd.items().size(), cmd.createdAt());

        } catch (InsufficientStockException ex) {
            log.warn("@ ReserveInventory: [STORE][FAILED] Inventory insufficient for order={} missing={} createdAt={}", cmd.orderNumber(), ex.getMissing(), cmd.createdAt());
            // mark out of stock
            reservationService.markInventoryOutOfStock(cmd, ex.getMissing());

        } catch (ConflictException ex) {
            // A reservation with this orderNumber already exists
            log.warn("@ ReserveInventory: [STORE][SKIPPED] reservation already done for order={} message={} createdAt={}", cmd.orderNumber(), ex.getMessage(), cmd.createdAt());

        } catch (CancelledByUserException ex) {
            log.warn("@ ReserveInventory: [SYS][SKIPPED] order already been cancelled for order={}, message={}, createdAt={}", cmd.orderNumber(), ex.getMessage(), cmd.createdAt());

        } catch (Exception ex) {
            log.error("@ ReserveInventory: [STORE][UNEXPECTED] Failed to reserve inventory for order={}: {}, createdAt={}", cmd.orderNumber(), ex.getMessage(), cmd.createdAt());
        }
    }

    // === Consume ReleaseInventory command
    // === Outbox InventoryReleased or InventoryReleasedRejected
    @KafkaHandler
    public void on(@Payload @Valid ReleaseInventory cmd) {
        try {
            reservationService.releaseReservation(cmd); // mark released
            log.info("@ ReleaseInventory: [STORE][SUCCESS] for order={} reason={} createdAt={}", cmd.orderNumber(), cmd.triggerBy(), cmd.createdAt());

        } catch (ResourceNotFoundException ex) {
            log.warn("@ ReleaseInventory: [STORE][NOOP] Reservation not found when releasing inventory for order={}, treating as idempotent, createdAt={}", cmd.orderNumber(), cmd.createdAt());
            reservationService.markOrphanInventoryReleased(cmd);

        } catch (ReleaseNotAllowedException ex) {
            log.warn("@ ReleaseInventory: [STORE][REJECTED] Reservation already committed={}, unable to release: {}, createdAt={}", cmd.orderNumber(), ex.getMessage(), cmd.createdAt());
            reservationService.markInventoryReleaseRejected(cmd);

        } catch (Exception ex) {
            log.error("@ ReleaseInventory: [STORE][UNEXPECTED] Failed to release inventory for order={}: {}, createdAt={}", cmd.orderNumber(), ex.getMessage(), cmd.createdAt());
        }
    }
}
