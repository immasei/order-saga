//package com.example.store.model;
//
//import com.example.store.model.User;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.UUID;
//
//@Entity
//@NoArgsConstructor
//@Getter
//@Setter
//public class AuditLog {
//
//    @Id
//    @GeneratedValue
//    private UUID id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "actor_user_id", referencedColumnName = "id", nullable = false)
//    private User actor;  // The user who performed the action
//
//    @Column(name = "entity_type", length = 80, nullable = false)
//    private String entityType;  // Type of entity being acted upon (e.g., 'ORDER', 'TRANSACTION')
//
//    @Column(name = "entity_id", nullable = false)
//    private UUID entityId;  // ID of the entity being acted upon
//
//    @Column(name = "action", length = 80, nullable = false)
//    private String action;  // Action performed (e.g., 'UPDATE', 'CREATE', 'DELETE')
//
//    @Column(name = "details", columnDefinition = "jsonb")
//    private String details;  // JSONB column to store any extra details (e.g., what exactly was changed)
//
//    @Column(name = "created_at", nullable = false)
//    private java.time.LocalDateTime createdAt;  // Timestamp of when the action occurred
//
//    // Constructor
//    public AuditLog(User actor, String entityType, UUID entityId, String action, String details, java.time.LocalDateTime createdAt) {
//        this.actor = actor;
//        this.entityType = entityType;
//        this.entityId = entityId;
//        this.action = action;
//        this.details = details;
//        this.createdAt = createdAt;
//    }
//
//    // Debugging method
//    @Override
//    public String toString() {
//        return "AuditLog{" +
//                "id=" + id +
//                ", actor=" + actor.getId() +
//                ", entityType='" + entityType + '\'' +
//                ", entityId=" + entityId +
//                ", action='" + action + '\'' +
//                ", details='" + details + '\'' +
//                ", createdAt=" + createdAt +
//                '}';
//    }
//}
