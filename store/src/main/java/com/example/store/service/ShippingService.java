package com.example.store.service;

import com.example.store.config.KafkaTopicProperties;
import com.example.store.dto.delivery.DeliveryDTO;
import com.example.store.dto.delivery.DeliveryResponseDTO;
import com.example.store.dto.delivery.DeliveryStatusCallbackDTO;
import com.example.store.dto.payment.ErrorResponse;
import com.example.store.enums.AggregateType;
import com.example.store.enums.OrderStatus;
import com.example.store.exception.CancelledByUserException;
import com.example.store.exception.DeliveryCoException;
import com.example.store.kafka.command.CreateShipment;
import com.example.store.kafka.event.DeliveryLost;
import com.example.store.kafka.event.ShipmentCreated;
import com.example.store.kafka.event.ShipmentFailed;
import com.example.store.model.Order;
import com.example.store.model.Outbox;
import com.example.store.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.EnumSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {

    @Qualifier("deliveryWebClient")
    private final WebClient deliveryWebClient;

    @Value("${delivery.loss.rate:0.0}") // fallback default = 0.0
    private double lossRate;

    private final KafkaTopicProperties kafkaProps;
    private final OutboxService outboxService;
    private final OrderRepository orderRepository;

    public void beforeShipment(CreateShipment cmd) {
        // check order status
        Order order = orderRepository
                .findByOrderNumberOrThrow(cmd.orderNumber());

        EnumSet<OrderStatus> allowed = EnumSet
                .of(OrderStatus.PAID_AND_AWAIT_SHIPMENT);

        if (!allowed.contains(order.getStatus())) {
            // Cancellation already requested or order moved on
            throw new CancelledByUserException("Already cancelled by customer");
        }
    }

    public DeliveryResponseDTO ship(CreateShipment cmd) {
        var req = new DeliveryDTO();
        req.setExternalOrderId(cmd.orderNumber());
        req.setCustomerId(cmd.customerId());
        req.setPickupLocations(cmd.pickupLocations());
        req.setDropoffAddress(cmd.dropOffAddress());
        req.setContactEmail(cmd.customerEmail());
        req.setLossRate(lossRate);
        req.setItems(cmd.productsByWarehouse());

        return deliveryWebClient.post()
            .uri("/api/deliveries")
            .bodyValue(req)
            .retrieve()
            // handle non-2xx responses
            .onStatus(HttpStatusCode::isError, resp ->
                resp.bodyToMono(ErrorResponse.class)
                    .map(err -> new DeliveryCoException(
                        err.getStatus(),
                        err.getMessage()
                    ))
            )
            .bodyToMono(DeliveryResponseDTO.class)
            .doOnNext(b -> log.info("@ CreateShipment: [DeliveryCo] response: {}", b))
            .timeout(Duration.ofSeconds(5))
            .block();
    }

    @Transactional
    public void markShipmentCreated(CreateShipment cmd, DeliveryResponseDTO delivery) {
        // 1. build evt
        ShipmentCreated evt = ShipmentCreated.of(cmd, delivery);
        // 2. outbox ChargePayment to db
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    @Transactional
    public void markShipmentFailed(CreateShipment cmd) {
        // 1. build evt
        ShipmentFailed evt = ShipmentFailed.of(cmd);
        // 2. outbox ChargePayment to db
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    private void emitEvent(String aggregateId, Class<?> type, Object payload) {
        Outbox outbox = new Outbox();
        outbox.setAggregateId(aggregateId);
        outbox.setAggregateType(AggregateType.SHIPMENT);
        outbox.setEventType(type.getName());
        outbox.setTopic(kafkaProps.shippingEvents());
        outboxService.save(outbox, payload);
    }

    public void updateDeliveryStatus(DeliveryStatusCallbackDTO dto) {
        Order order = orderRepository
                .findByOrderNumberForUpdateOrThrow(dto.externalOrderId());

        OrderStatus mappedStatus = mapToOrderStatus(dto.status());
        if (mappedStatus == null) return;

        order.setStatus(mappedStatus);
        Order saved = orderRepository.saveAndFlush(order);

        if (mappedStatus == OrderStatus.LOST_IN_DELIVERY) {
            // 1. build evt
            DeliveryLost evt = DeliveryLost.of(saved);
            // 2. outbox DeliveryLost to db
            emitEvent(evt.orderNumber(), evt.getClass(), evt);
        }

        log.info("[DeliveryCo] Delivery update: {} for order={}", mappedStatus, dto.externalOrderId());
    }

    private OrderStatus mapToOrderStatus(String status) {
        if (status == null) return null;
        return switch (status) {
            case "RECEIVED" -> OrderStatus.AWAIT_CARRIER_PICKUP;
            case "PICKED_UP", "IN_TRANSIT" -> OrderStatus.IN_TRANSIT;
            case "DELIVERED" -> OrderStatus.DELIVERED;
            case "LOST", "CANCELLED" -> OrderStatus.LOST_IN_DELIVERY;
            default -> null;
        };
    }
}
