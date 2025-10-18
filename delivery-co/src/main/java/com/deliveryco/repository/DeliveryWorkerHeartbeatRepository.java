package com.deliveryco.repository;

import com.deliveryco.entity.DeliveryWorkerHeartbeatEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryWorkerHeartbeatRepository extends JpaRepository<DeliveryWorkerHeartbeatEntity, UUID> {

    List<DeliveryWorkerHeartbeatEntity> findByRole(String role);
}

