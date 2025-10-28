package com.example.store.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "warehouse_stock")
public class Stock {
    @EmbeddedId
    private StockPK id = new StockPK();

    @MapsId("warehouseId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // total physical quantity of the product currently stored in that warehouse
    @Column(nullable = false)
    private int onHand;

    // portion of on_hand stock that has already been committed
    // to existing customer orders (but not yet shipped or released).
    @Column(nullable = false)
    private int reserved;

    public int getAvailableStock() {
        return onHand - reserved;
    }
}