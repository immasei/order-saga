//package com.example.store.model;
//
//import com.example.store.model.enums.TransactionType;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.UUID;
//
//@Entity
//@NoArgsConstructor
//@Getter
//@Setter
//public class TransactionRecord {
//
//    @Id
//    @GeneratedValue
//    private UUID id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
//    private CustomerOrder order;
//
//    @Column(name = "request_id", length = 100, unique = true, nullable = false)
//    private String requestId;  // Unique request identifier for the transaction
//
//    @Column(name = "status", length = 30, nullable = false)
//    private String status;  // Status of the transaction (e.g., 'PENDING', 'COMPLETED', 'FAILED')
//
//    @Column(name = "amount", nullable = false)
//    private long amount;  // Amount of the transaction in cents
//
//    @Column(name = "created_at", nullable = false)
//    private java.time.LocalDateTime createdAt;  // Timestamp when the transaction was created
//
//    @Column(name = "updated_at")
//    private java.time.LocalDateTime updatedAt;  // Timestamp for when the transaction was last updated
//
//    @Column(name = "transaction_type", length = 100, nullable = false)
//    @Enumerated(EnumType.STRING)
//    private TransactionType transactionType;  // Enum type (e.g., 'REFUND', 'SALE', etc.)
//
//    // Constructor
//    public TransactionRecord(CustomerOrder order, String requestId, String status, long amount,
//                             java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt, TransactionType transactionType) {
//        this.order = order;
//        this.requestId = requestId;
//        this.status = status;
//        this.amount = amount;
//        this.createdAt = createdAt;
//        this.updatedAt = updatedAt;
//        this.transactionType = transactionType;
//    }
//
//    // Debugging method
//    @Override
//    public String toString() {
//        return "TransactionRecord{" +
//                "id=" + id +
//                ", order=" + order.getId() +
//                ", requestId='" + requestId + '\'' +
//                ", status='" + status + '\'' +
//                ", amountCents=" + amount +
//                ", createdAt=" + createdAt +
//                ", updatedAt=" + updatedAt +
//                ", transactionType=" + transactionType +
//                '}';
//    }
//}
