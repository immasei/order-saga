package com.example.store.model;

import com.example.store.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "inventory_reservation",
    indexes = {
        @Index(name = "idx_inventory_reservation_order", columnList = "order_number", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
public class InventoryReservation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "order_number", length = 30, nullable = false, updatable = false)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.RESERVED;

    @Column(name = "idempotency_key", length = 80, nullable = false, updatable = false)
    private String idempotencyKey;

    @Column(name = "failure_reason")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryReservationItem> items = new ArrayList<>();

    public void addItem(InventoryReservationItem item) {
        item.setReservation(this);
        this.items.add(item);
    }

    public boolean isFinalised() {
        return status == ReservationStatus.RELEASED || status == ReservationStatus.COMMITTED;
    }
}


