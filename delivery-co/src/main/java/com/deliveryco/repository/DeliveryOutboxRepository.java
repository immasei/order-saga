package com.deliveryco.repository;

import com.deliveryco.domain.model.OutboxPublishState;
import com.deliveryco.entity.DeliveryOutboxEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryOutboxRepository extends JpaRepository<DeliveryOutboxEntity, UUID> {

    List<DeliveryOutboxEntity> findTop20ByPublishStateOrderByCreatedAtAsc(OutboxPublishState state);

    List<DeliveryOutboxEntity> findByPublishStateAndCreatedAtBefore(OutboxPublishState state, OffsetDateTime createdAt);
}

