package com.example.store.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class ProductPurchaseHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "purchase_count", nullable = false)
    private int purchaseCount;

    // Constructor
    public ProductPurchaseHistory(Product product, int purchaseCount) {
        this.product = product;
        this.purchaseCount = purchaseCount;
    }

    // Method for debugging
    @Override
    public String toString() {
        return "ProductPurchaseHistory{" +
                "id=" + id +
                ", product=" + product.getName() +
                ", purchaseCount=" + purchaseCount +
                '}';
    }
}
