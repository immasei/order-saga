package com.deliveryco.domain.service;

import com.deliveryco.config.properties.DeliverySchedulerProperties;
import com.deliveryco.domain.model.DeliveryIncidentType;
import com.deliveryco.domain.model.DeliveryItemStatus;
import com.deliveryco.domain.model.DeliveryJobState;
import com.deliveryco.domain.model.DeliveryJobType;
import com.deliveryco.domain.model.DeliveryOrderStatus;
import com.deliveryco.domain.service.support.LossSimulationService;
import com.deliveryco.entity.DeliveryJobEntity;
import com.deliveryco.entity.DeliveryOrderEntity;
import com.deliveryco.repository.DeliveryJobRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobExecutionService {

    private final DeliveryJobRepository jobRepository;
    private final DeliverySchedulerProperties schedulerProperties;
    private final StatusEventService statusEventService;
    private final LossSimulationService lossSimulationService;

    @Transactional
    public void processDueJobs() {
        OffsetDateTime now = OffsetDateTime.now();
        List<DeliveryJobEntity> dueJobs = jobRepository.findTop10ByStateAndRunAtBeforeOrderByRunAtAsc(
                DeliveryJobState.PENDING, now);
        dueJobs.forEach(this::executeJob);
    }

    private void executeJob(DeliveryJobEntity job) {
        var order = job.getDeliveryOrder();
        lockJob(job);
        try {
            switch (job.getJobType()) {
                case ACKNOWLEDGE_REQUEST -> acknowledgeRequest(order);
                case PICKUP_ORDER -> pickUpOrder(order);
                case START_TRANSIT -> startTransit(order);
                case COMPLETE_DELIVERY -> completeDelivery(order);
                case MARK_LOST -> markLost(order);
                case CANCEL_ORDER -> cancelOrder(order);
                case PUBLISH_OUTBOX -> log.debug("Outbox publishing handled by dedicated publisher.");
                default -> log.warn("Unhandled job type {}", job.getJobType());
            }
            job.setState(DeliveryJobState.COMPLETED);
        } catch (Exception ex) {
            log.error("Job {} failed", job.getId(), ex);
            job.setAttempt(job.getAttempt() + 1);
            job.setState(DeliveryJobState.FAILED);
        } finally {
            job.setUpdatedAt(OffsetDateTime.now());
        }
    }

    private void lockJob(DeliveryJobEntity job) {
        job.setState(DeliveryJobState.LOCKED);
        job.setLockOwner(schedulerProperties.workerId());
        job.setLockedAt(OffsetDateTime.now());
    }

    private void acknowledgeRequest(DeliveryOrderEntity order) {
        order.setAcknowledgedAt(OffsetDateTime.now());
        statusEventService.recordStatus(order, DeliveryOrderStatus.RECEIVED, "Delivery request acknowledged");
    }

    private void pickUpOrder(DeliveryOrderEntity order) {
        if (lossSimulationService.shouldLosePackage(order.getLossRate())) {
            handleLoss(order, DeliveryIncidentType.LOSS_AT_PICKUP, "Lost before pickup completed");
            return;
        }
        order.getItems().forEach(item -> item.setFulfillmentStatus(DeliveryItemStatus.EN_ROUTE));
        statusEventService.recordStatus(order, DeliveryOrderStatus.PICKED_UP, "Goods picked up from warehouse");
    }

    private void startTransit(DeliveryOrderEntity order) {
        if (order.getCurrentStatus() == DeliveryOrderStatus.LOST) {
            return;
        }
        if (lossSimulationService.shouldLosePackage(order.getLossRate())) {
            handleLoss(order, DeliveryIncidentType.LOSS_IN_TRANSIT, "Lost while in transit");
            return;
        }
        statusEventService.recordStatus(order, DeliveryOrderStatus.IN_TRANSIT, "Package out for delivery");
    }

    private void completeDelivery(DeliveryOrderEntity order) {
        if (order.getCurrentStatus() == DeliveryOrderStatus.LOST) {
            return;
        }
        if (lossSimulationService.shouldLosePackage(order.getLossRate())) {
            handleLoss(order, DeliveryIncidentType.LOSS_IN_TRANSIT, "Lost during final delivery");
            return;
        }
        order.getItems().forEach(item -> item.setFulfillmentStatus(DeliveryItemStatus.DELIVERED));
        statusEventService.recordStatus(order, DeliveryOrderStatus.DELIVERED, "Delivery completed");
    }

    private void markLost(DeliveryOrderEntity order) {
        handleLoss(order, DeliveryIncidentType.DELIVERY_EXCEPTION, "Marked lost by operator");
    }

    private void cancelOrder(DeliveryOrderEntity order) {
        if (order.isCancelled()) {
            return;
        }
        order.setCancelled(true);
        statusEventService.recordStatus(order, DeliveryOrderStatus.CANCELLED, "Delivery cancelled by store");
    }

    private void handleLoss(DeliveryOrderEntity order, DeliveryIncidentType type, String notes) {
        lossSimulationService.markItemsLost(order, type, notes);
        statusEventService.recordStatus(order, DeliveryOrderStatus.LOST, notes);
    }
}

