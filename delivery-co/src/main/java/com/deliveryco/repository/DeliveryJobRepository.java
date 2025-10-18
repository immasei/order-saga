package com.deliveryco.repository;

import com.deliveryco.domain.model.DeliveryJobState;
import com.deliveryco.entity.DeliveryJobEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface DeliveryJobRepository extends JpaRepository<DeliveryJobEntity, UUID> {

    List<DeliveryJobEntity> findTop10ByStateAndRunAtBeforeOrderByRunAtAsc(DeliveryJobState state, OffsetDateTime runAt);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select j from DeliveryJobEntity j where j.id = :id")
    Optional<DeliveryJobEntity> lockById(@Param("id") UUID id);
}

