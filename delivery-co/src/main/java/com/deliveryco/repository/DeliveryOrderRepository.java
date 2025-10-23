package com.deliveryco.repository;

import com.deliveryco.entity.DeliveryOrderEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrderEntity, UUID> {

    Optional<DeliveryOrderEntity> findByExternalOrderId(String externalOrderId);
}

