package com.example.store.repository;

import com.example.store.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    // Find audit logs by actor (user)
    List<AuditLog> findByActor_Id(UUID actorUserId);

    // Find audit logs by entity type and ID
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    // Find audit logs by action performed
    List<AuditLog> findByAction(String action);
}
