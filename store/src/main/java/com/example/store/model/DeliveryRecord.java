package com.example.store.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class DeliveryRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    private CustomerOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", referencedColumnName = "id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "tracking_number", length = 100, unique = true, nullable = false)
    private String trackingNumber;  // Unique tracking number for the shipment

    @Column(name = "status", length = 30, nullable = false)
    private String status;  // Status of the delivery (e.g., 'SHIPPED', 'IN_TRANSIT', 'DELIVERED')

    @Column(name = "shipped_at")
    private java.time.LocalDateTime shippedAt;  // Timestamp when the order was shipped

    @Column(name = "delivered_at")
    private java.time.LocalDateTime deliveredAt;  // Timestamp when the order was delivered

    // Constructor
    public DeliveryRecord(CustomerOrder order, Warehouse warehouse, String trackingNumber, String status, java.time.LocalDateTime shippedAt, java.time.LocalDateTime deliveredAt) {
        this.order = order;
        this.warehouse = warehouse;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
    }

    // Debugging method
    @Override
    public String toString() {
        return "DeliveryRecord{" +
                "id=" + id +
                ", order=" + order.getId() +
                ", warehouse=" + warehouse.getName() +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", status='" + status + '\'' +
                ", shippedAt=" + shippedAt +
                ", deliveredAt=" + deliveredAt +
                '}';
    }
}
