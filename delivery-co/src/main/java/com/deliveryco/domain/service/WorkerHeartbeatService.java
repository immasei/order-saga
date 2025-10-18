package com.deliveryco.domain.service;

import com.deliveryco.config.properties.DeliverySchedulerProperties;
import com.deliveryco.entity.DeliveryWorkerHeartbeatEntity;
import com.deliveryco.repository.DeliveryWorkerHeartbeatRepository;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkerHeartbeatService {

    private final DeliveryWorkerHeartbeatRepository heartbeatRepository;
    private final DeliverySchedulerProperties schedulerProperties;

    @Transactional
    public void beat(String role) {
        UUID nodeId = UUID.nameUUIDFromBytes((schedulerProperties.workerId() + "-" + role)
                .getBytes(StandardCharsets.UTF_8));
        DeliveryWorkerHeartbeatEntity entity = heartbeatRepository.findById(nodeId)
                .orElseGet(() -> DeliveryWorkerHeartbeatEntity.builder()
                        .nodeId(nodeId)
                        .role(role)
                        .status("ACTIVE")
                        .build());
        entity.setLastSeen(OffsetDateTime.now());
        entity.setStatus("ACTIVE");
        heartbeatRepository.save(entity);
    }
}

