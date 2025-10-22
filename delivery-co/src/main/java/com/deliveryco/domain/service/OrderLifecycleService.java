package com.deliveryco.domain.service;

import com.deliveryco.config.properties.DeliverySchedulerProperties;
import com.deliveryco.domain.model.DeliveryJobState;
import com.deliveryco.domain.model.DeliveryJobType;
import com.deliveryco.domain.model.DeliveryOrderStatus;
import com.deliveryco.domain.model.DeliveryRequest;
import com.deliveryco.domain.model.DeliveryRequestItem;
import com.deliveryco.entity.DeliveryItemEntity;
import com.deliveryco.entity.DeliveryJobEntity;
import com.deliveryco.entity.DeliveryOrderEntity;
import com.deliveryco.repository.DeliveryJobRepository;
import com.deliveryco.repository.DeliveryOrderRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderLifecycleService {

    private final DeliveryOrderRepository orderRepository;
    private final DeliveryJobRepository jobRepository;
    private final DeliverySchedulerProperties schedulerProperties;
    private final StatusEventService statusEventService;

    @Transactional
    public DeliveryOrderEntity registerDeliveryRequest(DeliveryRequest request) {
        return orderRepository.findByExternalOrderId(request.externalOrderId())
                .map(existing -> {
                    log.info("Delivery request {} already exists, returning existing aggregate.", request.externalOrderId());
                    return existing;
                })
                .orElseGet(() -> createNewOrder(request));
    }

    private DeliveryOrderEntity createNewOrder(DeliveryRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        double lossRate = request.determineLossRate(schedulerProperties.lossRateDefault());
        DeliveryOrderEntity order = DeliveryOrderEntity.builder()
                .id(UUID.randomUUID())
                .externalOrderId(request.externalOrderId())
                .customerId(request.customerId())
                .pickupWarehouseId(request.pickupWarehouseId())
                .pickupAddress(request.pickupAddress())
                .dropoffAddress(request.dropoffAddress())
                .contactEmail(request.contactEmail())
                .currentStatus(DeliveryOrderStatus.RECEIVED)
                .lossRate(lossRate)
                .requestedAt(now)
                .acknowledgedAt(null)
                .completedAt(null)
                .cancelled(false)
                .build();

        for (DeliveryRequestItem item : request.items()) {
            DeliveryItemEntity itemEntity = DeliveryItemEntity.builder()
                    .id(UUID.randomUUID())
                    .sku(item.sku())
                    .description(item.description())
                    .quantity(item.quantity())
                    .fulfillmentStatus(com.deliveryco.domain.model.DeliveryItemStatus.PENDING)
                    .build();
            order.addItem(itemEntity);
        }

        DeliveryOrderEntity savedOrder = orderRepository.save(order);
        statusEventService.recordStatus(savedOrder, DeliveryOrderStatus.RECEIVED, "Delivery request received");
        enqueueInitialJobs(savedOrder);
        return savedOrder;
    }

    private void enqueueInitialJobs(DeliveryOrderEntity order) {
        OffsetDateTime now = OffsetDateTime.now();
        saveJob(order, DeliveryJobType.ACKNOWLEDGE_REQUEST, now);
        saveJob(order, DeliveryJobType.PICKUP_ORDER, now.plus(schedulerProperties.defaultDelay()));
        saveJob(order, DeliveryJobType.START_TRANSIT, now.plus(schedulerProperties.defaultDelay().multipliedBy(2)));
        saveJob(order, DeliveryJobType.COMPLETE_DELIVERY, now.plus(schedulerProperties.defaultDelay().multipliedBy(3)));
    }

    private void saveJob(DeliveryOrderEntity order, DeliveryJobType type, OffsetDateTime runAt) {
        DeliveryJobEntity job = DeliveryJobEntity.builder()
                .id(UUID.randomUUID())
                .deliveryOrder(order)
                .jobType(type)
                .runAt(runAt)
                .attempt(0)
                .state(DeliveryJobState.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();

        jobRepository.save(job);
    }

}
