package com.example.store.kafka.saga;

import com.example.store.dto.delivery.DeliveryResponseDTO;
import com.example.store.dto.payment.PaymentResponseDTO;
import com.example.store.exception.BankException;
import com.example.store.exception.DeliveryException;
import com.example.store.kafka.command.CreateShipment;
import com.example.store.service.ShippingService;
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
    id = "shipping-handler",
    topics = "#{kafkaTopicProperties.shippingCommands()}",
    groupId = "shipping-handler"
)
@RequiredArgsConstructor
public class ShippingHandler {

    private final ShippingService shippingService;

    // === Consume CreateShipment command
    // === Outbox ShipmentCreated or ShipmentFailed
    @KafkaHandler
    public void on(@Payload @Valid CreateShipment cmd) {
        try {
            DeliveryResponseDTO delivery = shippingService.ship(cmd);
            shippingService.onShipmentCreated(cmd, delivery);
            log.info("@ CreateShipment: [DELIVERY-CO][SUCCESS] for order={}", cmd.orderNumber());

        } catch (DeliveryException ex) {
            log.warn("@ CreateShipment: [DELIVERY-CO][FAILED] for order={}, status={}, message={}", cmd.orderNumber(), ex.getStatusCode(), ex.getMessage());
            shippingService.onShipmentFailed(cmd);

        } catch (Exception ex) {
            log.error("@ CreateShipment: [DELIVERY-CO][UNEXPECTED] Failed to create delivery for order={}: {}", cmd.orderNumber(), ex.getMessage(), ex);
            throw ex;
        }

    }

}
