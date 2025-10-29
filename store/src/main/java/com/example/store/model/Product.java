package com.example.store.model;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(
    name = "products",
    indexes = @Index(name = "idx_product_code", columnList = "product_code", unique = true)
)
@ToString
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 30, unique = true, nullable = false, updatable = false)
    private String productCode;

    @Column(length = 200, nullable = false)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @PrePersist
    private void generateProductCode() {
        if (this.productCode == null) {
            this.productCode = "PRD-" + UlidCreator.getMonotonicUlid().toString();
        }
    }

}
