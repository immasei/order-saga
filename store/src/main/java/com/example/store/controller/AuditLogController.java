package com.example.store.controller;

import com.example.store.model.AuditLog;
import com.example.store.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auditlogs")
public class AuditLogController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Get all audit logs
    @GetMapping
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    // Get audit log by ID
    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable UUID id) {
        Optional<AuditLog> auditLog = auditLogRepository.findById(id);
        return auditLog.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get audit logs by actor (user ID)
    @GetMapping("/actor/{actorUserId}")
    public List<AuditLog> getAuditLogsByActor(@PathVariable UUID actorUserId) {
        return auditLogRepository.findAll()
                .stream()
                .filter(log -> log.getActor().getId().equals(actorUserId))
                .toList();
    }

    // Get audit logs by entity type and entity ID
    @GetMapping("/entity/{entityType}/{entityId}")
    public List<AuditLog> getAuditLogsByEntity(@PathVariable String entityType, @PathVariable UUID entityId) {
        return auditLogRepository.findAll()
                .stream()
                .filter(log -> log.getEntityType().equals(entityType) && log.getEntityId().equals(entityId))
                .toList();
    }

    // Create a new audit log
    @PostMapping
    public ResponseEntity<AuditLog> createAuditLog(@RequestBody AuditLog auditLog) {
        AuditLog createdAuditLog = auditLogRepository.save(auditLog);
        return ResponseEntity.ok(createdAuditLog);
    }

    // Update an audit log (usually for additional details or status changes)
    @PutMapping("/{id}")
    public ResponseEntity<AuditLog> updateAuditLog(@PathVariable UUID id, @RequestBody AuditLog auditLogDetails) {
        Optional<AuditLog> existingAuditLog = auditLogRepository.findById(id);
        if (existingAuditLog.isPresent()) {
            AuditLog auditLog = existingAuditLog.get();
            auditLog.setAction(auditLogDetails.getAction());  // Update action or other fields if necessary
            auditLog.setDetails(auditLogDetails.getDetails());
            auditLog.setCreatedAt(auditLogDetails.getCreatedAt());  // Ensure you update the timestamps appropriately
            auditLogRepository.save(auditLog);
            return ResponseEntity.ok(auditLog);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete an audit log by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuditLog(@PathVariable UUID id) {
        if (auditLogRepository.existsById(id)) {
            auditLogRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
