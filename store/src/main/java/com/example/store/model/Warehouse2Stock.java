package com.example.store.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Warehouse2Stock {

    @EmbeddedId
    private WarehouseStockId id;

    @ManyToOne
    @MapsId("warehouseId")
    @JoinColumn(name = "warehouse_id", referencedColumnName = "id", nullable = false)
    private Warehouse warehouse;

    @MapsId("productId")
    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @Column(name = "stock", nullable = false)
    private int stock;

    // Constructor
    public Warehouse2Stock(Warehouse warehouse, Product product, int stock) {
        this.warehouse = warehouse;
        this.product = product;
        this.stock = stock;
    }

    // Debugging method
    @Override
    public String toString() {
        return "Warehouse2Stock{" +
                "warehouse=" + warehouse.getName() +
                ", product=" + product.getName() +
                ", stock=" + stock +
                '}';
    }
}
