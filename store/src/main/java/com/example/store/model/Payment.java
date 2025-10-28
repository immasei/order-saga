package com.example.store.model;

import com.example.store.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(
    name = "payments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_order_id", columnNames = "order_id")
    },
    indexes = {
        @Index(name = "idx_payment_order_id", columnList = "order_id")
    }
)
public class Payment {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID orderId;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(length = 30, unique = true, nullable = false, updatable = false)
    private String providerTxnId;

    @Column(length=80, nullable=false, updatable=false)
    private String idempotencyKey;

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal refundedTotal = BigDecimal.ZERO;

    public BigDecimal refundable() {
        return amount.subtract(refundedTotal);
    }
}
