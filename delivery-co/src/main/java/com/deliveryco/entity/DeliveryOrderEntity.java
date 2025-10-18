package com.deliveryco.entity;

import com.deliveryco.domain.model.DeliveryOrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "delivery_order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryOrderEntity {

    @Id
    private UUID id;

    @Column(name = "external_order_id", nullable = false, unique = true, length = 64)
    private String externalOrderId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "pickup_warehouse_id", length = 64)
    private String pickupWarehouseId;

    @Column(name = "pickup_address")
    private String pickupAddress;

    @Column(name = "dropoff_address")
    private String dropoffAddress;

    @Column(name = "contact_email")
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 32)
    private DeliveryOrderStatus currentStatus;

    @Column(name = "loss_rate", nullable = false)
    private double lossRate;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "acknowledged_at")
    private OffsetDateTime acknowledgedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "cancelled", nullable = false)
    private boolean cancelled;

    @Version
    private long version;

    @OneToMany(mappedBy = "deliveryOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeliveryItemEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "deliveryOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeliveryStatusEventEntity> statusEvents = new ArrayList<>();

    @OneToMany(mappedBy = "deliveryOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeliveryJobEntity> jobs = new ArrayList<>();

    public void addItem(DeliveryItemEntity item) {
        items.add(item);
        item.setDeliveryOrder(this);
    }

    public void addStatusEvent(DeliveryStatusEventEntity event) {
        statusEvents.add(event);
        event.setDeliveryOrder(this);
    }

    public void addJob(DeliveryJobEntity job) {
        jobs.add(job);
        job.setDeliveryOrder(this);
    }
}
