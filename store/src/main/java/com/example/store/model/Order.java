package com.example.store.model;

import com.example.store.enums.OrderStatus;
import com.example.store.enums.ReservationStatus;
import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(
    name = "orders",
    indexes = @Index(name = "idx_orders_order_number", columnList = "order_number"),
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_orders_order_number", columnNames = "order_number"),
        @UniqueConstraint(name = "uk_orders_idempotency_key", columnNames = "idempotency_key")
    }
)
@ToString
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 30, unique = true, nullable = false, updatable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false)
    private User customer;

    @Column(nullable = false, length = 255, updatable = false)
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal subTotal;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal shipping;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal tax;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal total;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime placedAt;

    @Column(length=80, nullable=false, updatable=false, unique=true)
    private String idempotencyKey;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @PrePersist
    private void onCreate() {
        generateOrderNumber();
        computeSubTotal();
        computeTax();
        computeTotal();
    }

    private void computeTotal() {
        if (subTotal == null || shipping == null || tax == null) return;
        this.total = subTotal
                .add(shipping)
                .add(tax)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private void computeTax() {
        final BigDecimal TAX_RATE = new BigDecimal("0.10");
        if (subTotal == null) {
            this.tax = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return;
        }
        this.tax = subTotal
                .multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void computeSubTotal() {
        if (orderItems == null || orderItems.isEmpty()) {
            this.subTotal = BigDecimal.ZERO;
            return;
        }

        this.subTotal = orderItems.stream()
                .peek(OrderItem::computeLineTotal)
                .map(OrderItem::getLineTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void generateOrderNumber() {
        // if order number already given by dto -> ignore
        if (this.orderNumber == null) {
            // monotonic ULID = guaranteed unique + time sortable + thread-safe
            this.orderNumber = "ORD-" + UlidCreator.getMonotonicUlid().toString();
        }
    }

    public void addOrderItem(OrderItem item) {
        item.setOrder(this);
        this.orderItems.add(item);
    }

    public boolean isTerminal() {
        return switch (status) {
            case CANCELLED, SHIPPED, ERROR_DEAD_LETTER -> true;
            default -> false;
        };
    }


}
