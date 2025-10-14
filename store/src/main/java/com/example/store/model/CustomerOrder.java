package com.example.store.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class CustomerOrder {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "order_number", length = 30, unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Customer customer;

    @Column(name = "delivery_address", length = 255)
    private String deliveryAddress;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "sub_total")
    private long subTotal;

    @Column(name = "shipping")
    private long shipping;

    @Column(name = "tax")
    private long tax;

    @Column(name = "total")
    private long total;

    @Column(name = "placed_at")
    private java.time.LocalDateTime placedAt;

    @OneToMany(mappedBy = "orderDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItems> orderItems;

    // Constructor
    public CustomerOrder(String orderNumber, Customer customer, String deliveryAddress, String status,
                         long subTotal, long shipping, long tax, long total, java.time.LocalDateTime placedAt) {
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.subTotal = subTotal;
        this.shipping = shipping;
        this.tax = tax;
        this.total = total;
        this.placedAt = placedAt;
    }

    // Debugging method
    @Override
    public String toString() {
        return "OrderDetails{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", customer=" + customer.getEmail() +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", status='" + status + '\'' +
                ", subTotal=" + subTotal +
                ", shipping=" + shipping +
                ", tax=" + tax +
                ", total=" + total +
                ", placedAt=" + placedAt +
                '}';
    }
}
