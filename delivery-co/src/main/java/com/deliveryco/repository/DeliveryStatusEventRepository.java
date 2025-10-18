package com.deliveryco.repository;

import com.deliveryco.entity.DeliveryStatusEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryStatusEventRepository extends JpaRepository<DeliveryStatusEventEntity, UUID> {
    List<DeliveryStatusEventEntity> findByDeliveryOrderIdOrderByOccurredAt(UUID deliveryOrderId);
}

