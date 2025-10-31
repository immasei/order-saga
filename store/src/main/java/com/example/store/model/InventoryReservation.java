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
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "inventory_reservation",
    indexes = @Index(name = "idx_inventory_reservation_order", columnList = "order_number"),
    uniqueConstraints = {
        // One reservation record per order
        @UniqueConstraint(name = "uk_inventory_reservation_order", columnNames = "order_number"),
        // DB-level idempotency guard (UPSERT or catch duplicate on insert)
        @UniqueConstraint(name = "uk_inventory_reservation_idempo", columnNames = "idempotency_key")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class InventoryReservation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 30, nullable = false, updatable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(length = 80, nullable = false, updatable = false, unique = true)
    private String idempotencyKey;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
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

    public boolean isTerminal() {
        return EnumSet.of(
            ReservationStatus.RESERVED,
            ReservationStatus.COMMITTED,
            ReservationStatus.RELEASED,
            ReservationStatus.FAILED,
            ReservationStatus.CANCELLED
        ).contains(this.status);
    }
}


