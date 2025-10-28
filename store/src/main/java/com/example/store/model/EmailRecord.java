//package com.example.store.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.UUID;
//
//// Tbh idk if this record is necessary
//
//@Entity
//@NoArgsConstructor
//@Getter
//@Setter
//public class EmailRecord {
//
//    @Id
//    @GeneratedValue
//    private UUID id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
//    private Order order;
//
//    @Column(name = "to_address", length = 255, nullable = false)
//    private String toAddress;  // Email recipient address
//
//    @Column(name = "status", length = 20, nullable = false)
//    private String status;  // Email status (e.g., 'SENT', 'FAILED')
//
//    @Column(name = "created_at", nullable = false)
//    private java.time.LocalDateTime createdAt;  // Timestamp when the email was created
//
//    // Constructor
//    public EmailRecord(Order order, String toAddress, String status, java.time.LocalDateTime createdAt) {
//        this.order = order;
//        this.toAddress = toAddress;
//        this.status = status;
//        this.createdAt = createdAt;
//    }
//
//    // Debugging method
//    @Override
//    public String toString() {
//        return "EmailRecord{" +
//                "id=" + id +
//                ", order=" + order.getId() +
//                ", toAddress='" + toAddress + '\'' +
//                ", status='" + status + '\'' +
//                ", createdAt=" + createdAt +
//                '}';
//    }
//}
