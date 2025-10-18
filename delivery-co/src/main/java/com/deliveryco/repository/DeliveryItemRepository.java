package com.deliveryco.repository;

import com.deliveryco.entity.DeliveryItemEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryItemRepository extends JpaRepository<DeliveryItemEntity, UUID> {
}

