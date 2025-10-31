package com.example.store.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "inventory_reservation_item")
@Getter
@Setter
@NoArgsConstructor
public class InventoryReservationItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, updatable = false)
    private InventoryReservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false, updatable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;
}


