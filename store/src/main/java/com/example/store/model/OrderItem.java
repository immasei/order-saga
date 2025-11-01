package com.example.store.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(
    name = "order_item",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_orderitem_order_product",
        columnNames = {"order_id","product_id"}
    ),
    indexes = {
        @Index(name = "idx_orderitem_product", columnList = "product_id")
    }
)
public class OrderItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false)
    private Product product;

    @Column(nullable = false, length = 30, updatable = false)
    private String productCodeAtPurchase;

    @Column(length = 200, nullable = false, updatable = false)
    private String productNameAtPurchase;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal unitPrice; // price at time of purchase

    @Column(nullable = false, updatable = false)
    private int quantity = 0;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal lineTotal;

    @PrePersist
    private void onCreate() {
        computeLineTotal();
    }

    public void computeLineTotal() {
        if (unitPrice != null && quantity > 0) {
            this.lineTotal = unitPrice
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }

    public void increaseQuantity(int quantity) {
        this.quantity += quantity;
    }

}
