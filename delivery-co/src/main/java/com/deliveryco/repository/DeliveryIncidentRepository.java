package com.deliveryco.repository;

import com.deliveryco.entity.DeliveryIncidentEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryIncidentRepository extends JpaRepository<DeliveryIncidentEntity, UUID> {
}

